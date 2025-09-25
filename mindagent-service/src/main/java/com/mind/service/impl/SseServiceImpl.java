package com.mind.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mind.message.SseMessage;
import com.mind.service.SseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@AllArgsConstructor
public class SseServiceImpl implements SseService {

    private final ConcurrentMap<String, SseEmitter> clients = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public SseEmitter connect(String chatSessionId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        SseEmitter previous = clients.put(chatSessionId, emitter);
        if (previous != null) {
            previous.complete();
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("connected")
            );
        } catch (IOException | IllegalStateException e) {
            clients.remove(chatSessionId, emitter);
            emitter.completeWithError(e);
        }

        emitter.onCompletion(() -> {
            clients.remove(chatSessionId, emitter);
        });
        emitter.onTimeout(() -> clients.remove(chatSessionId, emitter));
        emitter.onError((error) -> clients.remove(chatSessionId, emitter));

        return emitter;
    }

    @Override
    public void send(String chatSessionId, SseMessage message) {
        SseEmitter emitter = clients.get(chatSessionId);

        if (emitter != null) {
            try {

                String sseMessageStr = objectMapper.writeValueAsString(message);
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(sseMessageStr)
                );
            } catch (IOException | IllegalStateException e) {
                clients.remove(chatSessionId, emitter);
                log.debug("SSE connection closed: {}", chatSessionId);
            }
        }
    }
}
