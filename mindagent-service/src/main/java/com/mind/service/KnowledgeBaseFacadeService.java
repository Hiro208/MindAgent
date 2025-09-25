package com.mind.service;

import com.mind.model.request.CreateKnowledgeBaseRequest;
import com.mind.model.request.UpdateKnowledgeBaseRequest;
import com.mind.model.response.CreateKnowledgeBaseResponse;
import com.mind.model.response.GetKnowledgeBasesResponse;

public interface KnowledgeBaseFacadeService {
    GetKnowledgeBasesResponse getKnowledgeBases();

    CreateKnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request);

    void deleteKnowledgeBase(String knowledgeBaseId);

    void updateKnowledgeBase(String knowledgeBaseId, UpdateKnowledgeBaseRequest request);
}

