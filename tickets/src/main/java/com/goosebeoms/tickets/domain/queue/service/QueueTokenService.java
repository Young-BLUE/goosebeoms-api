package com.goosebeoms.tickets.domain.queue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.queue.enabled", havingValue = "true")
public class QueueTokenService {

    public static final String TOKEN_KEY_PREFIX = "queue:token:";
    public static final String USER_TOKEN_KEY_PREFIX = "queue:user-token:";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final StringRedisTemplate redis;

    @Value("${app.queue.token-ttl-seconds}")
    private long tokenTtlSeconds;

    public IssuedToken issue(Long scheduleId, Long userId) {
        String tokenId = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + Duration.ofSeconds(tokenTtlSeconds).toMillis();
        TokenPayload payload = new TokenPayload(userId, scheduleId, expiresAt);

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("토큰 직렬화 실패", e);
        }

        Duration ttl = Duration.ofSeconds(tokenTtlSeconds);
        redis.opsForValue().set(TOKEN_KEY_PREFIX + tokenId, json, ttl);
        redis.opsForValue().set(userTokenKey(scheduleId, userId), tokenId, ttl);
        return new IssuedToken(tokenId, expiresAt);
    }

    public String findUserToken(Long scheduleId, Long userId) {
        return redis.opsForValue().get(userTokenKey(scheduleId, userId));
    }

    public void revoke(Long scheduleId, Long userId) {
        String tokenId = findUserToken(scheduleId, userId);
        if (tokenId != null) {
            redis.delete(TOKEN_KEY_PREFIX + tokenId);
        }
        redis.delete(userTokenKey(scheduleId, userId));
    }

    public void requireValid(String tokenId, Long scheduleId, Long userId) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new BusinessException(ErrorCode.QUEUE_TOKEN_REQUIRED);
        }
        String json = redis.opsForValue().get(TOKEN_KEY_PREFIX + tokenId);
        if (json == null) {
            throw new BusinessException(ErrorCode.QUEUE_TOKEN_EXPIRED);
        }
        TokenPayload payload;
        try {
            payload = objectMapper.readValue(json, TokenPayload.class);
        } catch (Exception e) {
            log.warn("Failed to parse queue token payload: {}", e.getMessage());
            throw new BusinessException(ErrorCode.QUEUE_TOKEN_EXPIRED);
        }
        if (!scheduleId.equals(payload.scheduleId()) || !userId.equals(payload.userId())) {
            throw new BusinessException(ErrorCode.QUEUE_TOKEN_MISMATCH);
        }
        if (payload.expiresAt() < System.currentTimeMillis()) {
            throw new BusinessException(ErrorCode.QUEUE_TOKEN_EXPIRED);
        }
    }

    private String userTokenKey(Long scheduleId, Long userId) {
        return USER_TOKEN_KEY_PREFIX + scheduleId + ":" + userId;
    }

    public record IssuedToken(String tokenId, long expiresAt) {}

    public record TokenPayload(Long userId, Long scheduleId, long expiresAt) {}
}
