package com.goosebeoms.tickets.domain.booking.event;

import com.goosebeoms.tickets.domain.notification.entity.Notification;

public record BookingNotificationEvent(
        Long userId,
        Notification.Type type,
        String title,
        String message,
        Long bookingId
) {}
