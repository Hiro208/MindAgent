package com.mind.agent.examples;

import com.mind.agent.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
public class MindAgentV1 {

    protected String name;

    protected String description;

    protected String systemPrompt;

    protected ChatClient chatClient;

    protected ChatMemory chatMemory;

    protected AgentState agentState;

    protected String sessionId;

    private static final Integer DEFAULT_MAX_MESSAGES = 20;

    public MindAgentV1() {}

    public MindAgentV1(String name,
                      String description,
                      String systemPrompt,
                      ChatClient chatClient,
                      Integer maxMessages,
                      String sessionId) {
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.chatClient = chatClient;
        this.sessionId = sessionId != null ? sessionId : "default-session";
        this.agentState = AgentState.IDLE;

        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(maxMessages != null ? maxMessages : DEFAULT_MAX_MESSAGES)
                .build();

        if (StringUtils.hasLength(systemPrompt)) {
            this.chatMemory.add(this.sessionId, new SystemMessage(systemPrompt));
        }
    }

    public String chat(String userInput) {
        Assert.notNull(userInput, "User input is required");

        if (agentState != AgentState.IDLE) {
            throw new IllegalStateException("Agent state is not IDLE，current state:" + agentState);
        }

        try {
            agentState = AgentState.THINKING;

            UserMessage userMessage = new UserMessage(userInput);
            chatMemory.add(sessionId, userMessage);

            Prompt prompt = Prompt.builder()
                    .messages(chatMemory.get(sessionId))
                    .build();

            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

            Assert.notNull(response, "ChatResponse must not be empty");

            AssistantMessage assistantMessage = response.getResult().getOutput();
            String aiResponse = assistantMessage.getText();

            chatMemory.add(sessionId, assistantMessage);

            agentState = AgentState.FINISHED;

            return aiResponse;

        } catch (Exception e) {
            agentState = AgentState.ERROR;
            log.error("An error occurred during the conversation", e);
            throw new RuntimeException("An error occurred during the conversation", e);
        } finally {

            agentState = AgentState.IDLE;
        }
    }

    public List<Message> getConversationHistory() {
        return chatMemory.get(sessionId);
    }

    public void reset() {
        chatMemory.clear(sessionId);
        if (StringUtils.hasLength(systemPrompt)) {
            chatMemory.add(sessionId, new SystemMessage(systemPrompt));
        }
        agentState = AgentState.IDLE;
    }

    @Override
    public String toString() {
        return "MindAgentV1 {" +
                "name = " + name + ",\n" +
                "description = " + description + ",\n" +
                "systemPrompt = " + systemPrompt + "}";
    }
}
