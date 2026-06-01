package com.goosebeoms.tickets.domain.user.dto;

import com.goosebeoms.tickets.domain.user.entity.User;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String email,
        String name,
        String phone,
        String role,
        String roleLabel,
        LocalDateTime createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole().name(),
                user.getRole().getLabel(),
                user.getCreatedAt()
        );
    }
}
