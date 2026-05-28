package com.goosebeoms.tickets.domain.notification.dto;

import com.goosebeoms.tickets.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String typeLabel,
        String title,
        String message,
        boolean isRead,
        String resourceType,
        Long resourceId,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType().name(),
                n.getType().getLabel(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getResourceType(),
                n.getResourceId(),
                n.getCreatedAt()
        );
    }
}
