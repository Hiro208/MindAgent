package com.kama.jchatmind.event.listener;

import com.kama.jchatmind.agent.MindAgent;
import com.kama.jchatmind.agent.MindAgentFactory;
import com.kama.jchatmind.event.ChatEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChatEventListener {

    private final MindAgentFactory mindAgentFactory;

    @Async
    @EventListener
    public void handle(ChatEvent event) {
        // 创建一个 Agent 实例处理聊天事件
        MindAgent mindAgent = mindAgentFactory.create(event.getAgentId(), event.getSessionId());
        mindAgent.run();
    }
}
