package com.goosebeoms.tickets.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redis;

    public void save(String jti, String email, long ttlSeconds) {
        redis.opsForValue().set(key(jti), email, Duration.ofSeconds(ttlSeconds));
    }

    public boolean exists(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(key(jti)));
    }

    public void revoke(String jti) {
        redis.delete(key(jti));
    }

    private String key(String jti) {
        return KEY_PREFIX + jti;
    }
}
