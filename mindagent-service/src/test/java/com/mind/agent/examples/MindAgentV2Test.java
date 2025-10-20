package com.mind.agent.examples;

import com.mind.agent.tools.demo.CityTool;
import com.mind.agent.tools.demo.DateTool;
import com.mind.agent.tools.demo.WeatherTool;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class MindAgentV2Test {

    @Autowired
    @Qualifier("deepseek-chat")
    private ChatClient chatClient;

    @Autowired
    private CityTool cityTool;

    @Autowired
    private DateTool dateTool;

    @Autowired
    private WeatherTool weatherTool;

    @Test
    public void testToolCalling() {

        ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(cityTool, dateTool, weatherTool)
                .build()
                .getToolCallbacks();

        MindAgentV2 agent = new MindAgentV2(
                "test-agent-v2",
                "Test Agent V2",
                "You are an assistant that helps users find weather, dates, and city information.",
                chatClient,
                20,
                "test-session-v2",
                Arrays.asList(toolCallbacks)
        );

        String userInput = "What is the weather like today?";
        String response = agent.chat(userInput);

        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("User input: " + userInput);
        System.out.println("AI Response: " + response);
        System.out.println("Conversation history length: " + agent.getConversationHistory().size());
    }

    @Test
    public void testMultipleToolCalls() {

        ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(cityTool, dateTool, weatherTool)
                .build()
                .getToolCallbacks();

        MindAgentV2 agent = new MindAgentV2(
                "test-agent-v2",
                "Test Agent V2",
                "You are an assistant that helps users find weather, dates, and city information.",
                chatClient,
                20,
                "test-session-v2-multi",
                Arrays.asList(toolCallbacks)
        );

        String userInput = "Tell me today's date and current city, then look up today's weather for that city.";
        String response = agent.chat(userInput);

        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("User input: " + userInput);
        System.out.println("AI Response: " + response);
        System.out.println("Conversation history length: " + agent.getConversationHistory().size());
    }

    @Test
    public void testConversationWithoutToolCalling() {

        MindAgentV2 agent = new MindAgentV2(
                "test-agent-v2",
                "Test Agent V2",
                "You are a friendly assistant.",
                chatClient,
                20,
                "test-session-v2-no-tool",
                List.of()
        );

        String userInput = "Hello, please introduce yourself.";
        String response = agent.chat(userInput);

        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("User input: " + userInput);
        System.out.println("AI Response: " + response);
    }

    @Test
    public void testReActLoop() {

        ToolCallback[] toolCallbacks = MethodToolCallbackProvider.builder()
                .toolObjects(cityTool, dateTool, weatherTool)
                .build()
                .getToolCallbacks();

        MindAgentV2 agent = new MindAgentV2(
                "test-agent-v2",
                "Test Agent V2",
                "You are an assistant that helps users find weather, dates, and city information.",
                chatClient,
                20,
                "test-session-v2-react",
                Arrays.asList(toolCallbacks)
        );

        String userInput = "I would like today's weather. First tell me today's date.";
        String response = agent.chat(userInput);

        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("User input: " + userInput);
        System.out.println("AI Response: " + response);
        System.out.println("Conversation history length: " + agent.getConversationHistory().size());
    }
}

