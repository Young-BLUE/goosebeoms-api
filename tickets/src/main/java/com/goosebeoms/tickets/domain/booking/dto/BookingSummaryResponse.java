package com.goosebeoms.tickets.domain.booking.dto;

import com.goosebeoms.tickets.domain.booking.entity.Booking;

import java.time.LocalDateTime;

public record BookingSummaryResponse(
        Long id,
        String showTitle,
        String venue,
        LocalDateTime scheduledAt,
        int seatCount,
        int finalPrice,
        String status,
        LocalDateTime createdAt
) {
    public static BookingSummaryResponse from(Booking booking) {
        return new BookingSummaryResponse(
                booking.getId(),
                booking.getShowSchedule().getShow().getTitle(),
                booking.getShowSchedule().getShow().getVenue(),
                booking.getShowSchedule().getScheduledAt(),
                booking.getBookingSeats().size(),
                booking.getFinalPrice(),
                booking.getStatus().name(),
                booking.getCreatedAt()
        );
    }
}
