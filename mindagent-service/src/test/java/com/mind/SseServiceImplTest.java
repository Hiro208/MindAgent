package com.mind;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mind.message.SseMessage;
import com.mind.service.impl.SseServiceImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SseServiceImplTest {
    @Test
    void disconnectedSessionDoesNotInterruptTask() {
        var service = new SseServiceImpl(new ObjectMapper());
        assertDoesNotThrow(() -> service.send("missing", SseMessage.builder().build()));
    }
}
