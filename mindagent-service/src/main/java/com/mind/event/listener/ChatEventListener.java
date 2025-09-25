package com.mind.event.listener;

import com.mind.agent.MindAgent;
import com.mind.agent.MindAgentFactory;
import com.mind.event.ChatEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import com.mind.service.ChatTaskDispatcher;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChatEventListener {

    private final MindAgentFactory mindAgentFactory;

    private final ChatTaskDispatcher dispatcher;

    @EventListener
    public void handle(ChatEvent event) {

        dispatcher.submit(event.getSessionId(), () -> {
            MindAgent mindAgent = mindAgentFactory.create(event.getAgentId(), event.getSessionId());
            mindAgent.run();
        });
    }
}
