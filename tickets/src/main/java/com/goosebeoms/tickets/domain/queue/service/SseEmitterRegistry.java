package com.goosebeoms.tickets.domain.queue.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.queue.enabled", havingValue = "true")
public class SseEmitterRegistry {

    private final Map<Long, Map<Long, SseEmitter>> byScheduleAndUser = new ConcurrentHashMap<>();

    public void register(Long scheduleId, Long userId, SseEmitter emitter) {
        Map<Long, SseEmitter> users = byScheduleAndUser.computeIfAbsent(scheduleId, k -> new ConcurrentHashMap<>());
        SseEmitter previous = users.put(userId, emitter);
        if (previous != null) {
            previous.complete();
        }
        emitter.onCompletion(() -> unregister(scheduleId, userId, emitter));
        emitter.onTimeout(() -> unregister(scheduleId, userId, emitter));
        emitter.onError(e -> unregister(scheduleId, userId, emitter));
    }

    public void unregister(Long scheduleId, Long userId, SseEmitter emitter) {
        Map<Long, SseEmitter> users = byScheduleAndUser.get(scheduleId);
        if (users != null) {
            users.remove(userId, emitter);
            if (users.isEmpty()) {
                byScheduleAndUser.remove(scheduleId, users);
            }
        }
    }

    public void send(Long scheduleId, Long userId, String eventName, Object data) {
        Map<Long, SseEmitter> users = byScheduleAndUser.get(scheduleId);
        if (users == null) return;
        SseEmitter emitter = users.get(userId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            log.debug("SSE send failed for user {} schedule {}: {}", userId, scheduleId, e.getMessage());
            unregister(scheduleId, userId, emitter);
        }
    }
}
