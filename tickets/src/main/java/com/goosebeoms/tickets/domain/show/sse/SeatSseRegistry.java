package com.goosebeoms.tickets.domain.show.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SeatSseRegistry {

    private final Map<Long, Set<SseEmitter>> byScheduleId = new ConcurrentHashMap<>();

    public void register(Long scheduleId, SseEmitter emitter) {
        Set<SseEmitter> subscribers = byScheduleId.computeIfAbsent(scheduleId,
                k -> ConcurrentHashMap.newKeySet());
        subscribers.add(emitter);
        emitter.onCompletion(() -> unregister(scheduleId, emitter));
        emitter.onTimeout(() -> unregister(scheduleId, emitter));
        emitter.onError(e -> unregister(scheduleId, emitter));
    }

    public void unregister(Long scheduleId, SseEmitter emitter) {
        Set<SseEmitter> subscribers = byScheduleId.get(scheduleId);
        if (subscribers == null) return;
        subscribers.remove(emitter);
        if (subscribers.isEmpty()) {
            byScheduleId.remove(scheduleId, subscribers);
        }
    }

    public void broadcast(Long scheduleId, String eventName, Object data) {
        Set<SseEmitter> subscribers = byScheduleId.get(scheduleId);
        if (subscribers == null || subscribers.isEmpty()) return;
        for (SseEmitter emitter : subscribers) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                log.debug("Seat SSE send failed for schedule {}: {}", scheduleId, e.getMessage());
                unregister(scheduleId, emitter);
            }
        }
    }
}
