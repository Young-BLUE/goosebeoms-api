package com.goosebeoms.tickets.global.security;

import com.goosebeoms.tickets.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private static final int MIN_SECRET_BYTES = 32;
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_VER = "ver";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @PostConstruct
    void validate() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes (set via JWT_SECRET env)."
            );
        }
    }

    public String generateAccess(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpiration))
                .signWith(key())
                .compact();
    }

    public IssuedRefresh generateRefresh(User user) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        long expiresAtMs = now.getTime() + refreshExpiration;
        String token = Jwts.builder()
                .id(jti)
                .subject(user.getEmail())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim(CLAIM_VER, user.getTokenVersion())
                .issuedAt(now)
                .expiration(new Date(expiresAtMs))
                .signWith(key())
                .compact();
        return new IssuedRefresh(token, jti, expiresAtMs);
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isValidAccess(String token) {
        try {
            Claims claims = getClaims(token);
            return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public ParsedRefresh parseRefresh(String token) {
        Claims claims = getClaims(token);
        if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("Not a refresh token");
        }
        Integer ver = claims.get(CLAIM_VER, Integer.class);
        return new ParsedRefresh(claims.getId(), claims.getSubject(), ver == null ? 0 : ver);
    }

    public long refreshTtlSeconds() {
        return refreshExpiration / 1000;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record IssuedRefresh(String token, String jti, long expiresAtMs) {}
    public record ParsedRefresh(String jti, String email, int tokenVersion) {}
}
