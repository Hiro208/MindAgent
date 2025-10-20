package com.mind;

import com.mind.service.ChatTaskDispatcher;
import com.mind.exception.BizException;
import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.concurrent.RejectedExecutionException;
import static org.junit.jupiter.api.Assertions.*;

class ChatTaskDispatcherTest {
    @Test
    void rejectsSameSessionUntilTaskCompletes() {
        var queue = new ArrayDeque<Runnable>();
        var dispatcher = new ChatTaskDispatcher(queue::add);
        dispatcher.submit("session", () -> {});
        assertThrows(BizException.class, () -> dispatcher.submit("session", () -> {}));
        queue.remove().run();
        assertDoesNotThrow(() -> dispatcher.submit("session", () -> {}));
    }

    @Test
    void releasesSessionAfterFailure() {
        var queue = new ArrayDeque<Runnable>();
        var dispatcher = new ChatTaskDispatcher(queue::add);
        dispatcher.submit("session", () -> { throw new IllegalStateException(); });
        assertThrows(IllegalStateException.class, () -> queue.remove().run());
        assertDoesNotThrow(() -> dispatcher.submit("session", () -> {}));
    }

    @Test
    void releasesSessionWhenExecutorRejectsTask() {
        var dispatcher = new ChatTaskDispatcher(task -> { throw new RejectedExecutionException(); });
        assertEquals("The service is busy. Please try again later.", assertThrows(BizException.class,
                () -> dispatcher.submit("session", () -> {})).getMessage());
        assertEquals("The service is busy. Please try again later.", assertThrows(BizException.class,
                () -> dispatcher.submit("session", () -> {})).getMessage());
    }
    @Test
    void rollbackSkipsTask() {
        var queue = new ArrayDeque<Runnable>();
        var dispatcher = new ChatTaskDispatcher(queue::add);
        var ran = new java.util.concurrent.atomic.AtomicBoolean();
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
        try {
            dispatcher.submit("session", () -> ran.set(true));
            org.springframework.transaction.support.TransactionSynchronizationManager.getSynchronizations()
                    .forEach(callback -> callback.afterCompletion(
                            org.springframework.transaction.support.TransactionSynchronization.STATUS_ROLLED_BACK));
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
        }
        queue.remove().run();
        assertFalse(ran.get());
        assertDoesNotThrow(() -> dispatcher.submit("session", () -> {}));
    }
}
