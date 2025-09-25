package com.mind.service;

import com.mind.model.request.CreateAgentRequest;
import com.mind.model.request.UpdateAgentRequest;
import com.mind.model.response.CreateAgentResponse;
import com.mind.model.response.GetAgentsResponse;

public interface AgentFacadeService {
    GetAgentsResponse getAgents();

    CreateAgentResponse createAgent(CreateAgentRequest request);

    void deleteAgent(String agentId);

    void updateAgent(String agentId, UpdateAgentRequest request);
}
