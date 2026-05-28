package com.goosebeoms.tickets.domain.notification.event;

import com.goosebeoms.tickets.domain.notification.dto.NotificationResponse;

public record NotificationCreatedEvent(Long userId, NotificationResponse payload) {}
