package com.goosebeoms.tickets.domain.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationSseRegistry {

    private final Map<Long, Set<SseEmitter>> byUserId = new ConcurrentHashMap<>();

    public void register(Long userId, SseEmitter emitter) {
        Set<SseEmitter> emitters = byUserId.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        emitters.add(emitter);
        emitter.onCompletion(() -> unregister(userId, emitter));
        emitter.onTimeout(() -> unregister(userId, emitter));
        emitter.onError(e -> unregister(userId, emitter));
    }

    public void unregister(Long userId, SseEmitter emitter) {
        Set<SseEmitter> emitters = byUserId.get(userId);
        if (emitters == null) return;
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            byUserId.remove(userId, emitters);
        }
    }

    public void send(Long userId, String eventName, Object data) {
        Set<SseEmitter> emitters = byUserId.get(userId);
        if (emitters == null || emitters.isEmpty()) return;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                log.debug("Notification SSE send failed for user {}: {}", userId, e.getMessage());
                unregister(userId, emitter);
            }
        }
    }
}
