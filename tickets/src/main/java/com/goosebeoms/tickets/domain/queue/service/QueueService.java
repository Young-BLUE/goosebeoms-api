package com.goosebeoms.tickets.domain.queue.service;

import com.goosebeoms.tickets.domain.queue.dto.QueueStatusResponse;
import com.goosebeoms.tickets.domain.show.repository.ShowScheduleRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class QueueService {

    public static final String WAIT_KEY_PREFIX = "queue:wait:";
    public static final String ACTIVE_KEY_PREFIX = "queue:active:";
    private static final Duration WAIT_KEY_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;
    private final QueueTokenService tokenService;
    private final UserRepository userRepository;
    private final ShowScheduleRepository scheduleRepository;

    @Value("${app.queue.seconds-per-person}")
    private long secondsPerPerson;

    public QueueStatusResponse enter(Long scheduleId, String email) {
        Long userId = userIdOrThrow(email);
        scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        QueueStatusResponse activeResponse = activeOrNull(scheduleId, userId);
        if (activeResponse != null) return activeResponse;

        String waitKey = waitKey(scheduleId);
        String userIdStr = userId.toString();
        redis.opsForZSet().addIfAbsent(waitKey, userIdStr, System.currentTimeMillis());
        redis.expire(waitKey, WAIT_KEY_TTL);
        return positionStatus(scheduleId, userId);
    }

    public QueueStatusResponse status(Long scheduleId, String email) {
        Long userId = userIdOrThrow(email);

        QueueStatusResponse activeResponse = activeOrNull(scheduleId, userId);
        if (activeResponse != null) return activeResponse;

        Long rank = redis.opsForZSet().rank(waitKey(scheduleId), userId.toString());
        if (rank == null) return QueueStatusResponse.none();
        return positionStatus(scheduleId, userId);
    }

    public void leave(Long scheduleId, String email) {
        Long userId = userIdOrThrow(email);
        redis.opsForZSet().remove(waitKey(scheduleId), userId.toString());
    }

    private QueueStatusResponse activeOrNull(Long scheduleId, Long userId) {
        Double score = redis.opsForZSet().score(activeKey(scheduleId), userId.toString());
        if (score == null) return null;
        if (score < System.currentTimeMillis()) return null;
        String tokenId = tokenService.findUserToken(scheduleId, userId);
        if (tokenId == null) return null;
        return QueueStatusResponse.active(tokenId, score.longValue());
    }

    private QueueStatusResponse positionStatus(Long scheduleId, Long userId) {
        Long rank = redis.opsForZSet().rank(waitKey(scheduleId), userId.toString());
        if (rank == null) return QueueStatusResponse.none();
        long position = rank + 1;
        long ahead = rank;
        long eta = ahead * secondsPerPerson;
        return QueueStatusResponse.waiting(position, ahead, eta);
    }

    private Long userIdOrThrow(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }

    public static String waitKey(Long scheduleId) {
        return WAIT_KEY_PREFIX + scheduleId;
    }

    public static String activeKey(Long scheduleId) {
        return ACTIVE_KEY_PREFIX + scheduleId;
    }
}
