package com.goosebeoms.tickets.domain.queue.controller;

import com.goosebeoms.tickets.domain.queue.service.QueueService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dev / Queue", description = "데모용 대기열 ghost 시드 (dev 전용)")
@SecurityRequirements
@Profile("dev")
@RestController
@RequestMapping("/dev/queue")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.queue.enabled", havingValue = "true")
public class DevQueueController {

    private static final int MAX_SEED = 10000;
    private static final double GHOST_SCORE_UPPER = -1.0;

    private final StringRedisTemplate redis;

    @Operation(summary = "ghost 시드", description = "음수 ID 가짜 대기자를 대기열 앞쪽에 추가. 기존 ghost는 교체.")
    @PostMapping("/{scheduleId}/seed")
    public ApiResponse<SeedResult> seed(
            @PathVariable Long scheduleId,
            @RequestParam(defaultValue = "100") int count
    ) {
        if (count <= 0 || count > MAX_SEED) {
            throw new IllegalArgumentException("count must be 1.." + MAX_SEED);
        }
        String waitKey = QueueService.waitKey(scheduleId);
        Long removed = redis.opsForZSet()
                .removeRangeByScore(waitKey, Double.NEGATIVE_INFINITY, GHOST_SCORE_UPPER);
        for (int i = 1; i <= count; i++) {
            redis.opsForZSet().add(waitKey, "-" + i, -i);
        }
        Long total = redis.opsForZSet().zCard(waitKey);
        return ApiResponse.ok(new SeedResult(count, removed == null ? 0 : removed, total == null ? 0 : total));
    }

    @Operation(summary = "ghost 전체 제거", description = "음수 score 멤버만 제거. 실제 사용자 대기열은 유지.")
    @DeleteMapping("/{scheduleId}/seed")
    public ApiResponse<SeedResult> clear(@PathVariable Long scheduleId) {
        String waitKey = QueueService.waitKey(scheduleId);
        Long removed = redis.opsForZSet()
                .removeRangeByScore(waitKey, Double.NEGATIVE_INFINITY, GHOST_SCORE_UPPER);
        Long total = redis.opsForZSet().zCard(waitKey);
        return ApiResponse.ok(new SeedResult(0, removed == null ? 0 : removed, total == null ? 0 : total));
    }

    public record SeedResult(int seeded, long removedGhosts, long totalWaiting) {}
}
