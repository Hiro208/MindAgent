package com.mind.agent.examples;

import com.mind.agent.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class MindAgentV2 extends MindAgentV1 {

    protected List<ToolCallback> availableTools;

    protected ToolCallingManager toolCallingManager;

    // ChatOptions
    protected ChatOptions chatOptions;

    protected ChatResponse lastChatResponse;

    private static final Integer MAX_STEPS = 20;

    public MindAgentV2() {
        super();
    }

    public MindAgentV2(String name,
                      String description,
                      String systemPrompt,
                      org.springframework.ai.chat.client.ChatClient chatClient,
                      Integer maxMessages,
                      String sessionId,
                      List<ToolCallback> availableTools) {
        super(name, description, systemPrompt, chatClient, maxMessages, sessionId);
        this.availableTools = availableTools;

        this.chatOptions = DefaultToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();

        this.toolCallingManager = ToolCallingManager.builder().build();
    }

    protected void logToolCalls(List<AssistantMessage.ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            log.info("\n\n[ToolCalling] No tool calls");
            return;
        }
        String logMessage = IntStream.range(0, toolCalls.size())
                .mapToObj(i -> {
                    AssistantMessage.ToolCall call = toolCalls.get(i);
                    return String.format(
                            "[ToolCalling #%d]\n- name      : %s\n- arguments : %s",
                            i + 1,
                            call.name(),
                            call.arguments()
                    );
                })
                .collect(Collectors.joining("\n\n"));
        log.info("\n\n========== Tool Calling ==========\n{}\n=================================\n", logMessage);
    }

    protected boolean think() {
        String thinkPrompt = """
                You are an intelligent decision module.
                Choose the next action based on the conversation context.
                Call the appropriate tools when needed to complete the task.
                """;

        Prompt prompt = Prompt.builder()
                .chatOptions(this.chatOptions)
                .messages(chatMemory.get(sessionId))
                .build();

        this.lastChatResponse = chatClient
                .prompt(prompt)
                .system(thinkPrompt)
                .toolCallbacks(availableTools != null ? availableTools.toArray(new ToolCallback[0]) : new ToolCallback[0])
                .call()
                .chatClientResponse()
                .chatResponse();

        Assert.notNull(lastChatResponse, "Last chat response cannot be null");

        AssistantMessage output = this.lastChatResponse
                .getResult()
                .getOutput();

        List<AssistantMessage.ToolCall> toolCalls = output.getToolCalls();

        logToolCalls(toolCalls);

        if (toolCalls.isEmpty()) {
            chatMemory.add(sessionId, output);
        }

        return !toolCalls.isEmpty();
    }

    protected void execute() {
        Assert.notNull(this.lastChatResponse, "Last chat response cannot be null");

        if (!this.lastChatResponse.hasToolCalls()) {
            return;
        }

        Prompt prompt = Prompt.builder()
                .messages(chatMemory.get(sessionId))
                .chatOptions(this.chatOptions)
                .build();

        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, this.lastChatResponse);

        chatMemory.clear(sessionId);
        chatMemory.add(sessionId, toolExecutionResult.conversationHistory());

        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult
                .conversationHistory()
                .get(toolExecutionResult.conversationHistory().size() - 1);

        String result = toolResponseMessage.getResponses()
                .stream()
                .map(resp -> "Tool " + resp.name() + "  returned:" + resp.responseData())
                .collect(Collectors.joining("\n"));

        if (toolResponseMessage.getResponses()
                .stream()
                .anyMatch(resp -> resp.name().equals("terminate"))) {
            this.agentState = AgentState.FINISHED;
            log.info("Task complete");
        }
    }

    protected void step() {
        if (think()) {

            execute();
        } else {

            agentState = AgentState.FINISHED;
        }
    }

    @Override
    public String chat(String userInput) {
        Assert.notNull(userInput, "User input is required");

        if (agentState != AgentState.IDLE) {
            throw new IllegalStateException("Agent state is not IDLE，current state:" + agentState);
        }

        try {
            agentState = AgentState.THINKING;

            UserMessage userMessage = new UserMessage(userInput);
            chatMemory.add(sessionId, userMessage);

            for (int i = 0; i < MAX_STEPS && agentState != AgentState.FINISHED; i++) {
                step();
                if (i >= MAX_STEPS - 1) {
                    agentState = AgentState.FINISHED;
                    log.warn("Maximum step count reached; stopping Agent");
                }
            }

            List<Message> history = chatMemory.get(sessionId);
            String aiResponse = "";
            for (int i = history.size() - 1; i >= 0; i--) {
                Message msg = history.get(i);
                if (msg instanceof AssistantMessage) {
                    aiResponse = ((AssistantMessage) msg).getText();
                    break;
                }
            }

            agentState = AgentState.FINISHED;

            return aiResponse;
        } catch (Exception e) {
            agentState = AgentState.ERROR;
            log.error("Agent execution failed", e);
            throw new RuntimeException("Agent execution failed", e);
        } finally {

            agentState = AgentState.IDLE;
        }
    }
}
