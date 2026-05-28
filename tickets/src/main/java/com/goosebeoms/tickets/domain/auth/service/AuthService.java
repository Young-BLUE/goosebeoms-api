package com.goosebeoms.tickets.domain.auth.service;

import com.goosebeoms.tickets.domain.auth.dto.AuthResponse;
import com.goosebeoms.tickets.domain.auth.dto.LoginRequest;
import com.goosebeoms.tickets.domain.auth.dto.PasswordChangeRequest;
import com.goosebeoms.tickets.domain.auth.dto.SignupRequest;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import com.goosebeoms.tickets.global.security.JwtProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }

        User user = userRepository.save(User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .build());

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return AuthResponse.profile(user);
    }

    public AuthResponse refresh(String refreshToken) {
        JwtProvider.ParsedRefresh parsed;
        try {
            parsed = jwtProvider.parseRefresh(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!refreshTokenStore.exists(parsed.jti())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByEmail(parsed.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (user.getTokenVersion() != parsed.tokenVersion()) {
            refreshTokenStore.revoke(parsed.jti());
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        refreshTokenStore.revoke(parsed.jti());
        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        try {
            JwtProvider.ParsedRefresh parsed = jwtProvider.parseRefresh(refreshToken);
            refreshTokenStore.revoke(parsed.jti());
        } catch (JwtException | IllegalArgumentException ignored) {
            // 만료/위조 토큰이라도 멱등하게 통과 — 이미 무효이므로 사실상 로그아웃 상태
        }
    }

    public void changePassword(String email, PasswordChangeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        // tokenVersion 증가로 기존 모든 refresh token 자동 무효화
    }

    private AuthResponse issueTokens(User user) {
        String access = jwtProvider.generateAccess(user);
        JwtProvider.IssuedRefresh refresh = jwtProvider.generateRefresh(user);
        refreshTokenStore.save(refresh.jti(), user.getEmail(), jwtProvider.refreshTtlSeconds());
        return AuthResponse.of(access, refresh.token(), user);
    }
}
