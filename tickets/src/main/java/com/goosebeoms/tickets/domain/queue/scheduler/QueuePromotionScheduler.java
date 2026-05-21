package com.goosebeoms.tickets.domain.queue.scheduler;

import com.goosebeoms.tickets.domain.queue.dto.QueueStatusResponse;
import com.goosebeoms.tickets.domain.queue.service.QueueService;
import com.goosebeoms.tickets.domain.queue.service.QueueTokenService;
import com.goosebeoms.tickets.domain.queue.service.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.queue.enabled", havingValue = "true")
public class QueuePromotionScheduler {

    private static final String LEADER_LOCK_KEY = "queue:promote:lock";
    private static final long SCAN_COUNT = 100;
    private static final long SEND_RANK_TOP_N = 100;

    private final StringRedisTemplate redis;
    private final QueueTokenService tokenService;
    private final SseEmitterRegistry sseRegistry;
    private final ObjectProvider<RedissonClient> redissonProvider;

    @Value("${app.queue.active-capacity}")
    private int activeCapacity;

    @Scheduled(fixedDelayString = "${app.queue.promotion-interval-ms}")
    public void promote() {
        RedissonClient redisson = redissonProvider.getIfAvailable();
        if (redisson == null) {
            promoteAll();
            return;
        }
        RLock lock = redisson.getLock(LEADER_LOCK_KEY);
        try {
            if (!lock.tryLock(0, 3, TimeUnit.SECONDS)) return;
            promoteAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    private void promoteAll() {
        Set<Long> scheduleIds = scanScheduleIds();
        for (Long scheduleId : scheduleIds) {
            try {
                promoteSchedule(scheduleId);
            } catch (Exception e) {
                log.warn("Promotion failed for schedule {}: {}", scheduleId, e.getMessage());
            }
        }
    }

    private void promoteSchedule(Long scheduleId) {
        String waitKey = QueueService.waitKey(scheduleId);
        String activeKey = QueueService.activeKey(scheduleId);
        long now = System.currentTimeMillis();

        redis.opsForZSet().removeRangeByScore(activeKey, 0, now);

        Long activeCount = redis.opsForZSet().zCard(activeKey);
        long currentActive = activeCount == null ? 0L : activeCount;
        long slots = activeCapacity - currentActive;
        if (slots <= 0) {
            pushRanks(scheduleId);
            return;
        }

        Set<String> promoted = redis.opsForZSet().range(waitKey, 0, slots - 1);
        if (promoted == null || promoted.isEmpty()) return;

        for (String userIdStr : promoted) {
            Long userId = parseLong(userIdStr);
            if (userId == null) continue;
            QueueTokenService.IssuedToken issued = tokenService.issue(scheduleId, userId);
            redis.opsForZSet().add(activeKey, userIdStr, issued.expiresAt());
            redis.opsForZSet().remove(waitKey, userIdStr);
            sseRegistry.send(scheduleId, userId, "ready",
                    QueueStatusResponse.active(issued.tokenId(), issued.expiresAt()));
        }
        pushRanks(scheduleId);
    }

    private void pushRanks(Long scheduleId) {
        String waitKey = QueueService.waitKey(scheduleId);
        Set<String> top = redis.opsForZSet().range(waitKey, 0, SEND_RANK_TOP_N - 1);
        if (top == null || top.isEmpty()) return;
        long position = 1;
        for (String userIdStr : top) {
            Long userId = parseLong(userIdStr);
            if (userId == null) { position++; continue; }
            sseRegistry.send(scheduleId, userId, "rank",
                    QueueStatusResponse.waiting(position, position - 1, (position - 1) * 2));
            position++;
        }
    }

    private Set<Long> scanScheduleIds() {
        Set<Long> ids = new HashSet<>();
        ScanOptions waitScan = ScanOptions.scanOptions().match(QueueService.WAIT_KEY_PREFIX + "*").count(SCAN_COUNT).build();
        try (Cursor<String> cursor = redis.scan(waitScan)) {
            cursor.forEachRemaining(k -> {
                Long id = parseLong(k.substring(QueueService.WAIT_KEY_PREFIX.length()));
                if (id != null) ids.add(id);
            });
        }
        ScanOptions activeScan = ScanOptions.scanOptions().match(QueueService.ACTIVE_KEY_PREFIX + "*").count(SCAN_COUNT).build();
        try (Cursor<String> cursor = redis.scan(activeScan)) {
            cursor.forEachRemaining(k -> {
                Long id = parseLong(k.substring(QueueService.ACTIVE_KEY_PREFIX.length()));
                if (id != null) ids.add(id);
            });
        }
        return ids;
    }

    private Long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    public void runOnce() {
        promoteAll();
    }
}
