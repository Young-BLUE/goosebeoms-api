package com.goosebeoms.tickets.domain.auth.dto;

import com.goosebeoms.tickets.domain.user.entity.User;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String name,
        String role,
        String roleLabel
) {
    public static AuthResponse of(String accessToken, String refreshToken, User user) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getRole().getLabel()
        );
    }

    public static AuthResponse profile(User user) {
        return new AuthResponse(null, null, user.getEmail(), user.getName(),
                user.getRole().name(), user.getRole().getLabel());
    }
}
