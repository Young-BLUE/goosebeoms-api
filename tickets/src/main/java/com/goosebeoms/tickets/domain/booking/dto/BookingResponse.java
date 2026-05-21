package com.goosebeoms.tickets.domain.booking.dto;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.entity.BookingSeat;

import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        String showTitle,
        String venue,
        LocalDateTime scheduledAt,
        List<SeatInfo> seats,
        int originalPrice,
        int discountPrice,
        int finalPrice,
        String status,
        String statusLabel,
        LocalDateTime createdAt
) {
    public record SeatInfo(String zone, String rowLabel, int number, int price) {
        public static SeatInfo from(BookingSeat bs) {
            return new SeatInfo(
                    bs.getSeat().getZone().getName(),
                    bs.getSeat().getRowLabel(),
                    bs.getSeat().getNumber(),
                    bs.getPrice()
            );
        }
    }

    public static BookingResponse from(Booking booking, List<BookingSeat> bookingSeats) {
        return new BookingResponse(
                booking.getId(),
                booking.getShowSchedule().getShow().getTitle(),
                booking.getShowSchedule().getShow().getVenue(),
                booking.getShowSchedule().getScheduledAt(),
                bookingSeats.stream().map(SeatInfo::from).toList(),
                booking.getOriginalPrice(),
                booking.getDiscountPrice(),
                booking.getFinalPrice(),
                booking.getStatus().name(),
                booking.getStatus().getLabel(),
                booking.getCreatedAt()
        );
    }
}
