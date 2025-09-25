package com.mind.service;

import com.mind.model.request.CreateChatSessionRequest;
import com.mind.model.request.UpdateChatSessionRequest;
import com.mind.model.response.CreateChatSessionResponse;
import com.mind.model.response.GetChatSessionResponse;
import com.mind.model.response.GetChatSessionsResponse;

public interface ChatSessionFacadeService {
    GetChatSessionsResponse getChatSessions();

    GetChatSessionResponse getChatSession(String chatSessionId);

    GetChatSessionsResponse getChatSessionsByAgentId(String agentId);

    CreateChatSessionResponse createChatSession(CreateChatSessionRequest request);

    void deleteChatSession(String chatSessionId);

    void updateChatSession(String chatSessionId, UpdateChatSessionRequest request);
}
