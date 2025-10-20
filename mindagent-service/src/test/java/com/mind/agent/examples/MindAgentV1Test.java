package com.mind.agent.examples;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class MindAgentV1Test {

    @Autowired
    @Qualifier("deepseek-chat")
    private ChatClient chatClient;

    @Test
    public void testBasicChat() {

        MindAgentV1 agent = new MindAgentV1(
                "test-agent-v1",
                "Test Agent V1",
                "You are a friendly assistant.",
                chatClient,
                20,
                "test-session-v1"
        );

        String userInput = "Hello, please introduce yourself.";
        String response = agent.chat(userInput);

        assertNotNull(response);
        assertTrue(response.length() > 0);

        System.out.println("User input: " + userInput);
        System.out.println("AI Response: " + response);
        System.out.println("Conversation history length: " + agent.getConversationHistory().size());
    }

    @Test
    public void testMultiTurnConversation() {

        MindAgentV1 agent = new MindAgentV1(
                "test-agent-v1",
                "Test Agent V1",
                "",
                chatClient,
                20,
                "test-session-v1-multi"
        );

        String response1 = agent.chat("My name is Alex");
        assertNotNull(response1);
        System.out.println("Round 1 - [User]: Is my name Alex?");
        System.out.println("Round 1 - [AI]: " + response1);

        String response2 = agent.chat("What is my name?");
        assertNotNull(response2);
        System.out.println("Round 2 - [User]: What is my name?");
        System.out.println("Round 2 - [AI]: " + response2);

        assertTrue(agent.getConversationHistory().size() >= 4);
    }

    @Test
    public void testResetConversation() {

        MindAgentV1 agent = new MindAgentV1(
                "test-agent-v1",
                "Test Agent V1",
                "You are a helpful assistant.",
                chatClient,
                20,
                "test-session-v1-reset"
        );

        agent.chat("Hello");
        int historySizeBeforeReset = agent.getConversationHistory().size();

        agent.reset();

        int historySizeAfterReset = agent.getConversationHistory().size();
        assertTrue(historySizeAfterReset < historySizeBeforeReset);
        assertTrue(historySizeAfterReset <= 1);

        System.out.println("History length before reset: " + historySizeBeforeReset);
        System.out.println("History length after reset: " + historySizeAfterReset);
    }
}

