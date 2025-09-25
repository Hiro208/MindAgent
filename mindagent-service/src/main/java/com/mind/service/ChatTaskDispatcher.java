package com.mind.service;

import com.mind.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.concurrent.CompletableFuture;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Service
@RequiredArgsConstructor
public class ChatTaskDispatcher {
    private final Executor taskExecutor;
    private final Set<String> activeSessions = ConcurrentHashMap.newKeySet();

    public void submit(String sessionId, Runnable task) {
        if (!activeSessions.add(sessionId)) {
            throw new BizException("This conversation is processing a request. Please try again later.");
        }
        CompletableFuture<Boolean> committed = new CompletableFuture<>();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    committed.complete(status == STATUS_COMMITTED);
                }
            });
        } else {
            committed.complete(true);
        }
        try {
            taskExecutor.execute(() -> {
                try {
                    if (committed.join()) {
                        task.run();
                    }
                } finally {
                    activeSessions.remove(sessionId);
                }
            });
        } catch (RejectedExecutionException e) {
            activeSessions.remove(sessionId);
            throw new BizException("The service is busy. Please try again later.");
        }
    }
}
