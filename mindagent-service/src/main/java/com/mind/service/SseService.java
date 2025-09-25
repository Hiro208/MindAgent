package com.mind.service;

import com.mind.message.SseMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

    SseEmitter connect(String chatSessionId);

    void send(String chatSessionId, SseMessage message);
}
