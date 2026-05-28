package com.goosebeoms.tickets.global.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String KEY_PREFIX = "rate:";

    private final StringRedisTemplate redis;

    @Around("@annotation(rateLimit)")
    public Object enforce(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String key = KEY_PREFIX + rateLimit.bucket() + ":" + resolveKey(rateLimit.keyType());

        Long count = redis.opsForValue().increment(key);
        if (count == null) {
            log.warn("Rate limiter Redis returned null count for {}", key);
            return pjp.proceed();
        }
        if (count == 1L) {
            redis.expire(key, Duration.ofSeconds(rateLimit.windowSeconds()));
        }
        if (count > rateLimit.limit()) {
            Long ttl = redis.getExpire(key);
            long retry = (ttl == null || ttl < 0) ? rateLimit.windowSeconds() : ttl;
            throw new RateLimitExceededException(retry);
        }
        return pjp.proceed();
    }

    private String resolveKey(RateLimit.KeyType type) {
        return switch (type) {
            case IP -> "ip:" + clientIp();
            case USER -> "user:" + authenticatedPrincipal();
        };
    }

    private String clientIp() {
        HttpServletRequest req = currentRequest();
        if (req == null) return "unknown";
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private String authenticatedPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anon:" + clientIp();
        }
        return auth.getName();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }
}
