package com.mind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mind.converter.KnowledgeBaseConverter;
import com.mind.exception.BizException;
import com.mind.mapper.KnowledgeBaseMapper;
import com.mind.model.dto.KnowledgeBaseDTO;
import com.mind.model.entity.KnowledgeBase;
import com.mind.model.request.CreateKnowledgeBaseRequest;
import com.mind.model.request.UpdateKnowledgeBaseRequest;
import com.mind.model.response.CreateKnowledgeBaseResponse;
import com.mind.model.response.GetKnowledgeBasesResponse;
import com.mind.model.vo.KnowledgeBaseVO;
import com.mind.service.KnowledgeBaseFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class KnowledgeBaseFacadeServiceImpl implements KnowledgeBaseFacadeService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeBaseConverter knowledgeBaseConverter;

    @Override
    public GetKnowledgeBasesResponse getKnowledgeBases() {
        List<KnowledgeBase> knowledgeBases = knowledgeBaseMapper.selectAll();
        List<KnowledgeBaseVO> result = new ArrayList<>();
        for (KnowledgeBase knowledgeBase : knowledgeBases) {
            try {
                KnowledgeBaseVO vo = knowledgeBaseConverter.toVO(knowledgeBase);
                result.add(vo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetKnowledgeBasesResponse.builder()
                .knowledgeBases(result.toArray(new KnowledgeBaseVO[0]))
                .build();
    }

    @Override
    public CreateKnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request) {
        try {

            KnowledgeBaseDTO knowledgeBaseDTO = knowledgeBaseConverter.toDTO(request);

            KnowledgeBase knowledgeBase = knowledgeBaseConverter.toEntity(knowledgeBaseDTO);

            LocalDateTime now = LocalDateTime.now();
            knowledgeBase.setCreatedAt(now);
            knowledgeBase.setUpdatedAt(now);

            int result = knowledgeBaseMapper.insert(knowledgeBase);
            if (result <= 0) {
                throw new BizException("Failed to create knowledge base");
            }

            return CreateKnowledgeBaseResponse.builder()
                    .knowledgeBaseId(knowledgeBase.getId())
                    .build();
        } catch (JsonProcessingException e) {
            throw new BizException("Create knowledge base serialization failed: " + e.getMessage());
        }
    }

    @Override
    public void deleteKnowledgeBase(String knowledgeBaseId) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new BizException("Knowledge base does not exist: " + knowledgeBaseId);
        }

        int result = knowledgeBaseMapper.deleteById(knowledgeBaseId);
        if (result <= 0) {
            throw new BizException("Failed to delete knowledge base");
        }
    }

    @Override
    public void updateKnowledgeBase(String knowledgeBaseId, UpdateKnowledgeBaseRequest request) {
        try {

            KnowledgeBase existingKnowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
            if (existingKnowledgeBase == null) {
                throw new BizException("Knowledge base does not exist: " + knowledgeBaseId);
            }

            KnowledgeBaseDTO knowledgeBaseDTO = knowledgeBaseConverter.toDTO(existingKnowledgeBase);

            knowledgeBaseConverter.updateDTOFromRequest(knowledgeBaseDTO, request);

            KnowledgeBase updatedKnowledgeBase = knowledgeBaseConverter.toEntity(knowledgeBaseDTO);

            updatedKnowledgeBase.setId(existingKnowledgeBase.getId());
            updatedKnowledgeBase.setCreatedAt(existingKnowledgeBase.getCreatedAt());
            updatedKnowledgeBase.setUpdatedAt(LocalDateTime.now());

            int result = knowledgeBaseMapper.updateById(updatedKnowledgeBase);
            if (result <= 0) {
                throw new BizException("Failed to update knowledge base");
            }
        } catch (JsonProcessingException e) {
            throw new BizException("Update knowledge base serialization failed: " + e.getMessage());
        }
    }
}
