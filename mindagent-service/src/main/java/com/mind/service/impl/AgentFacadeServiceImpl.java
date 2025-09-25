package com.mind.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mind.converter.AgentConverter;
import com.mind.exception.BizException;
import com.mind.mapper.AgentMapper;
import com.mind.model.dto.AgentDTO;
import com.mind.model.entity.Agent;
import com.mind.model.request.CreateAgentRequest;
import com.mind.model.request.UpdateAgentRequest;
import com.mind.model.response.CreateAgentResponse;
import com.mind.model.response.GetAgentsResponse;
import com.mind.model.vo.AgentVO;
import com.mind.service.AgentFacadeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AgentFacadeServiceImpl implements AgentFacadeService {

    private final AgentMapper agentMapper;
    private final AgentConverter agentConverter;

    @Override
    public GetAgentsResponse getAgents() {
        List<Agent> agents = agentMapper.selectAll();
        List<AgentVO> result = new ArrayList<>();
        for (Agent agent : agents) {
            try {
                AgentVO vo = agentConverter.toVO(agent);
                result.add(vo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return GetAgentsResponse.builder()
                .agents(result.toArray(new AgentVO[0]))
                .build();
    }

    @Override
    public CreateAgentResponse createAgent(CreateAgentRequest request) {
        try {

            AgentDTO agentDTO = agentConverter.toDTO(request);

            Agent agent = agentConverter.toEntity(agentDTO);

            LocalDateTime now = LocalDateTime.now();
            agent.setCreatedAt(now);
            agent.setUpdatedAt(now);

            int result = agentMapper.insert(agent);
            if (result <= 0) {
                throw new BizException("Failed to create agent");
            }

            return CreateAgentResponse.builder()
                    .agentId(agent.getId())
                    .build();
        } catch (JsonProcessingException e) {
            throw new BizException("Failed to serialize agent creation request: " + e.getMessage());
        }
    }

    @Override
    public void deleteAgent(String agentId) {
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new BizException("Agent does not exist: " + agentId);
        }

        int result = agentMapper.deleteById(agentId);
        if (result <= 0) {
            throw new BizException("Failed to delete agent");
        }
    }

    @Override
    public void updateAgent(String agentId, UpdateAgentRequest request) {
        try {

            Agent existingAgent = agentMapper.selectById(agentId);
            if (existingAgent == null) {
                throw new BizException("Agent does not exist: " + agentId);
            }

            AgentDTO agentDTO = agentConverter.toDTO(existingAgent);

            agentConverter.updateDTOFromRequest(agentDTO, request);

            Agent updatedAgent = agentConverter.toEntity(agentDTO);

            updatedAgent.setId(existingAgent.getId());
            updatedAgent.setCreatedAt(existingAgent.getCreatedAt());
            updatedAgent.setUpdatedAt(LocalDateTime.now());

            int result = agentMapper.updateById(updatedAgent);
            if (result <= 0) {
                throw new BizException("Failed to update agent");
            }
        } catch (JsonProcessingException e) {
            throw new BizException("Failed to serialize agent update request: " + e.getMessage());
        }
    }
}
