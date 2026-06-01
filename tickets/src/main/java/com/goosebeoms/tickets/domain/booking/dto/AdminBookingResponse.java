package com.goosebeoms.tickets.domain.booking.dto;

import com.goosebeoms.tickets.domain.booking.entity.Booking;

import java.time.LocalDateTime;

public record AdminBookingResponse(
        Long id,
        Long userId,
        String userEmail,
        String userName,
        Long scheduleId,
        String showTitle,
        String venue,
        LocalDateTime scheduledAt,
        int seatCount,
        int originalPrice,
        int discountPrice,
        int finalPrice,
        String status,
        String statusLabel,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
    public static AdminBookingResponse from(Booking booking) {
        return new AdminBookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getEmail(),
                booking.getUser().getName(),
                booking.getShowSchedule().getId(),
                booking.getShowSchedule().getShow().getTitle(),
                booking.getShowSchedule().getShow().getVenue(),
                booking.getShowSchedule().getScheduledAt(),
                booking.getBookingSeats().size(),
                booking.getOriginalPrice(),
                booking.getDiscountPrice(),
                booking.getFinalPrice(),
                booking.getStatus().name(),
                booking.getStatus().getLabel(),
                booking.getCreatedAt(),
                booking.getPaidAt()
        );
    }
}
