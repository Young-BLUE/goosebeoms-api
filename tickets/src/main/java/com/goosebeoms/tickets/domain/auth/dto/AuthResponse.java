package com.goosebeoms.tickets.domain.auth.dto;

import com.goosebeoms.tickets.domain.user.entity.User;

public record AuthResponse(
        String token,
        String email,
        String name,
        String role
) {
    public static AuthResponse of(String token, User user) {
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }
}
