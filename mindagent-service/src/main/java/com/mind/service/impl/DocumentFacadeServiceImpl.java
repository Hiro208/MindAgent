package com.mind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mind.converter.DocumentConverter;
import com.mind.exception.BizException;
import com.mind.mapper.DocumentMapper;
import com.mind.model.dto.DocumentDTO;
import com.mind.model.entity.Document;
import com.mind.model.request.CreateDocumentRequest;
import com.mind.model.request.UpdateDocumentRequest;
import com.mind.model.response.CreateDocumentResponse;
import com.mind.model.response.GetDocumentsResponse;
import com.mind.model.vo.DocumentVO;
import com.mind.mapper.ChunkBgeM3Mapper;
import com.mind.model.entity.ChunkBgeM3;
import com.mind.service.DocumentFacadeService;
import com.mind.service.DocumentStorageService;
import com.mind.service.MarkdownParserService;
import com.mind.service.RagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentFacadeServiceImpl implements DocumentFacadeService {

    private final DocumentMapper documentMapper;
    private final DocumentConverter documentConverter;
    private final DocumentStorageService documentStorageService;
    private final MarkdownParserService markdownParserService;
    private final RagService ragService;
    private final ChunkBgeM3Mapper chunkBgeM3Mapper;

    @Override
    public GetDocumentsResponse getDocuments() {
        List<Document> documents = documentMapper.selectAll();
        List<DocumentVO> result = new ArrayList<>();
        for (Document document : documents) {
            try {
                DocumentVO vo = documentConverter.toVO(document);
                result.add(vo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetDocumentsResponse.builder()
                .documents(result.toArray(new DocumentVO[0]))
                .build();
    }

    @Override
    public GetDocumentsResponse getDocumentsByKbId(String kbId) {
        List<Document> documents = documentMapper.selectByKbId(kbId);
        List<DocumentVO> result = new ArrayList<>();
        for (Document document : documents) {
            try {
                DocumentVO vo = documentConverter.toVO(document);
                result.add(vo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetDocumentsResponse.builder()
                .documents(result.toArray(new DocumentVO[0]))
                .build();
    }

    @Override
    public CreateDocumentResponse createDocument(CreateDocumentRequest request) {
        try {

            DocumentDTO documentDTO = documentConverter.toDTO(request);

            Document document = documentConverter.toEntity(documentDTO);

            LocalDateTime now = LocalDateTime.now();
            document.setCreatedAt(now);
            document.setUpdatedAt(now);

            int result = documentMapper.insert(document);
            if (result <= 0) {
                throw new BizException("Failed to create document");
            }

            return CreateDocumentResponse.builder()
                    .documentId(document.getId())
                    .build();
        } catch (JsonProcessingException e) {
            throw new BizException("Create document serialization failed: " + e.getMessage());
        }
    }

    @Override
    public CreateDocumentResponse uploadDocument(String kbId, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new BizException("Uploaded file is empty");
            }

            String originalFilename = file.getOriginalFilename();
            String filetype = getFileType(originalFilename);
            long fileSize = file.getSize();

            DocumentDTO documentDTO = DocumentDTO.builder()
                    .kbId(kbId)
                    .filename(originalFilename)
                    .filetype(filetype)
                    .size(fileSize)
                    .build();

            Document document = documentConverter.toEntity(documentDTO);
            LocalDateTime now = LocalDateTime.now();
            document.setCreatedAt(now);
            document.setUpdatedAt(now);

            int result = documentMapper.insert(document);
            if (result <= 0) {
                throw new BizException("Failed to create document record");
            }

            String documentId = document.getId();

            String filePath = documentStorageService.saveFile(kbId, documentId, file);

            DocumentDTO.MetaData metadata = new DocumentDTO.MetaData();
            metadata.setFilePath(filePath);
            documentDTO.setMetadata(metadata);
            documentDTO.setId(documentId);
            documentDTO.setCreatedAt(now);
            documentDTO.setUpdatedAt(now);

            Document updatedDocument = documentConverter.toEntity(documentDTO);
            updatedDocument.setId(documentId);
            updatedDocument.setCreatedAt(now);
            updatedDocument.setUpdatedAt(now);

            documentMapper.updateById(updatedDocument);

            log.info("Document uploaded: kbId={}, documentId={}, filename={}", kbId, documentId, originalFilename);

            if ("md".equalsIgnoreCase(filetype) || "markdown".equalsIgnoreCase(filetype)) {
                processMarkdownDocument(kbId, documentId, filePath);
            } else {

                log.warn("Unsupported file type: {}", filetype);
            }

            return CreateDocumentResponse.builder()
                    .documentId(documentId)
                    .build();
        } catch (IOException e) {
            log.error("Failed to save file", e);
            throw new BizException("Failed to save file: " + e.getMessage());
        }
    }

    @Override
    public void deleteDocument(String documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BizException("Document does not exist: " + documentId);
        }

        try {
            DocumentDTO documentDTO = documentConverter.toDTO(document);
            if (documentDTO.getMetadata() != null && documentDTO.getMetadata().getFilePath() != null) {
                String filePath = documentDTO.getMetadata().getFilePath();
                documentStorageService.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.warn("File deletion failed; continuing to delete the document record: documentId={}, error={}", documentId, e.getMessage());

        }

        int result = documentMapper.deleteById(documentId);
        if (result <= 0) {
            throw new BizException("Failed to delete document");
        }
    }

    private void processMarkdownDocument(String kbId, String documentId, String filePath) {
        try {
            log.info("Processing Markdown document: kbId={}, documentId={}, filePath={}", kbId, documentId, filePath);

            Path path = documentStorageService.getFilePath(filePath);
            try (InputStream inputStream = Files.newInputStream(path)) {

                List<MarkdownParserService.MarkdownSection> sections = markdownParserService.parseMarkdown(inputStream);

                System.out.println(sections);

                if (sections.isEmpty()) {
                    log.warn("Markdown document contains no sections: documentId={}", documentId);
                    return;
                }

                LocalDateTime now = LocalDateTime.now();
                int chunkCount = 0;

                for (MarkdownParserService.MarkdownSection section : sections) {
                    String title = section.getTitle();
                    String content = section.getContent();

                    if (title == null || title.trim().isEmpty()) {
                        continue;
                    }

                    float[] embedding = ragService.embed(title);

                    ChunkBgeM3 chunk = ChunkBgeM3.builder()
                            .kbId(kbId)
                            .docId(documentId)
                            .content(content != null ? content : "")
                            .metadata(null)
                            .embedding(embedding)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    int result = chunkBgeM3Mapper.insert(chunk);

                    if (result > 0) {
                        chunkCount++;
                        log.debug("Chunk created: title={}, chunkId={}", title, chunk.getId());
                    } else {
                        log.warn("Failed to create chunk: title={}", title);
                    }
                }
                log.info("Markdown document processing complete: documentId={}, generated {} chunks", documentId, chunkCount);
            }
        } catch (Exception e) {
            log.error("Failed to process Markdown document: documentId={}", documentId, e);

        }
    }

    private String getFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    @Override
    public void updateDocument(String documentId, UpdateDocumentRequest request) {
        try {

            Document existingDocument = documentMapper.selectById(documentId);
            if (existingDocument == null) {
                throw new BizException("Document does not exist: " + documentId);
            }

            DocumentDTO documentDTO = documentConverter.toDTO(existingDocument);

            documentConverter.updateDTOFromRequest(documentDTO, request);

            Document updatedDocument = documentConverter.toEntity(documentDTO);

            updatedDocument.setId(existingDocument.getId());
            updatedDocument.setKbId(existingDocument.getKbId());
            updatedDocument.setCreatedAt(existingDocument.getCreatedAt());
            updatedDocument.setUpdatedAt(LocalDateTime.now());

            int result = documentMapper.updateById(updatedDocument);
            if (result <= 0) {
                throw new BizException("Failed to update document");
            }
        } catch (JsonProcessingException e) {
            throw new BizException("Update document serialization failed: " + e.getMessage());
        }
    }
}
