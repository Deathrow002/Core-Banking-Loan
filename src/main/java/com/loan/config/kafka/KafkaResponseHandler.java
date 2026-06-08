package com.loan.config.kafka;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(KafkaResponseHandler.class);

    private final ConcurrentHashMap<String, CompletableFuture<String>> responseMap = new ConcurrentHashMap<>();

    public void register(String correlationId, CompletableFuture<String> future) {
        log.debug("Registering future for correlation ID: {}", correlationId);
        responseMap.put(correlationId, future);
    }

    public void complete(String correlationId, String response) {
        CompletableFuture<String> future = responseMap.remove(correlationId);
        if (future != null) {
            future.complete(response);
        }
    }
}
