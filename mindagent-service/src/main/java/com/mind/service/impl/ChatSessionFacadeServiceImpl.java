package com.mind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mind.converter.ChatSessionConverter;
import com.mind.exception.BizException;
import com.mind.mapper.ChatSessionMapper;
import com.mind.model.dto.ChatSessionDTO;
import com.mind.model.entity.ChatSession;
import com.mind.model.request.CreateChatSessionRequest;
import com.mind.model.request.UpdateChatSessionRequest;
import com.mind.model.response.CreateChatSessionResponse;
import com.mind.model.response.GetChatSessionResponse;
import com.mind.model.response.GetChatSessionsResponse;
import com.mind.model.vo.ChatSessionVO;
import com.mind.service.ChatSessionFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatSessionFacadeServiceImpl implements ChatSessionFacadeService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatSessionConverter chatSessionConverter;

    @Override
    public GetChatSessionsResponse getChatSessions() {
        List<ChatSession> chatSessions = chatSessionMapper.selectAll();
        List<ChatSessionVO> result = new ArrayList<>();
        for (ChatSession chatSession : chatSessions) {
            try {
                ChatSessionVO vo = chatSessionConverter.toVO(chatSession);
                result.add(vo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetChatSessionsResponse.builder()
                .chatSessions(result.toArray(new ChatSessionVO[0]))
                .build();
    }

    @Override
    public GetChatSessionResponse getChatSession(String chatSessionId) {
        ChatSession chatSession = chatSessionMapper.selectById(chatSessionId);
        if (chatSession != null) {
            try {
                ChatSessionVO vo = chatSessionConverter.toVO(chatSession);
                return GetChatSessionResponse.builder()
                        .chatSession(vo)
                        .build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        throw new BizException("Chat session does not exist: " + chatSessionId);
    }

    @Override
    public GetChatSessionsResponse getChatSessionsByAgentId(String agentId) {
        List<ChatSession> chatSessions = chatSessionMapper.selectByAgentId(agentId);
        List<ChatSessionVO> result = new ArrayList<>();
        for (ChatSession chatSession : chatSessions) {
            try {
                ChatSessionVO vo = chatSessionConverter.toVO(chatSession);
                result.add(vo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetChatSessionsResponse.builder()
                .chatSessions(result.toArray(new ChatSessionVO[0]))
                .build();
    }

    @Override
    public CreateChatSessionResponse createChatSession(CreateChatSessionRequest request) {
        try {

            ChatSessionDTO chatSessionDTO = chatSessionConverter.toDTO(request);

            ChatSession chatSession = chatSessionConverter.toEntity(chatSessionDTO);

            LocalDateTime now = LocalDateTime.now();
            chatSession.setCreatedAt(now);
            chatSession.setUpdatedAt(now);

            int result = chatSessionMapper.insert(chatSession);
            if (result <= 0) {
                throw new BizException("Failed to create chat session");
            }

            return CreateChatSessionResponse.builder()
                    .chatSessionId(chatSession.getId())
                    .build();
        } catch (JsonProcessingException e) {
            throw new BizException("Create chat session serialization failed: " + e.getMessage());
        }
    }

    @Override
    public void deleteChatSession(String chatSessionId) {
        ChatSession chatSession = chatSessionMapper.selectById(chatSessionId);
        if (chatSession == null) {
            throw new BizException("Chat session does not exist: " + chatSessionId);
        }

        int result = chatSessionMapper.deleteById(chatSessionId);
        if (result <= 0) {
            throw new BizException("Failed to delete chat session");
        }
    }

    @Override
    public void updateChatSession(String chatSessionId, UpdateChatSessionRequest request) {
        try {

            ChatSession existingChatSession = chatSessionMapper.selectById(chatSessionId);
            if (existingChatSession == null) {
                throw new BizException("Chat session does not exist: " + chatSessionId);
            }

            ChatSessionDTO chatSessionDTO = chatSessionConverter.toDTO(existingChatSession);

            chatSessionConverter.updateDTOFromRequest(chatSessionDTO, request);

            ChatSession updatedChatSession = chatSessionConverter.toEntity(chatSessionDTO);

            updatedChatSession.setId(existingChatSession.getId());
            updatedChatSession.setAgentId(existingChatSession.getAgentId());
            updatedChatSession.setCreatedAt(existingChatSession.getCreatedAt());
            updatedChatSession.setUpdatedAt(LocalDateTime.now());

            int result = chatSessionMapper.updateById(updatedChatSession);
            if (result <= 0) {
                throw new BizException("Failed to update chat session");
            }
        } catch (JsonProcessingException e) {
            throw new BizException("Update chat session serialization failed: " + e.getMessage());
        }
    }
}
