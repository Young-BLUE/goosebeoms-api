package com.goosebeoms.tickets.domain.booking.dto;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.entity.BookingSeat;
import com.goosebeoms.tickets.domain.payment.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;

public record AdminBookingDetailResponse(
        Long id,
        Long userId,
        String userEmail,
        String userName,
        Long scheduleId,
        String showTitle,
        String venue,
        LocalDateTime scheduledAt,
        List<SeatInfo> seats,
        int originalPrice,
        int discountPrice,
        int finalPrice,
        String status,
        String statusLabel,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        PaymentInfo payment
) {
    public record SeatInfo(Long id, String zone, String rowLabel, int number, int price) {
        public static SeatInfo from(BookingSeat bs) {
            return new SeatInfo(
                    bs.getSeat().getId(),
                    bs.getSeat().getZone().getName(),
                    bs.getSeat().getRowLabel(),
                    bs.getSeat().getNumber(),
                    bs.getPrice()
            );
        }
    }

    public record PaymentInfo(
            Long id,
            String orderId,
            int amount,
            String method,
            String status,
            String statusLabel,
            int refundedAmount,
            String cancelReason,
            LocalDateTime paidAt,
            LocalDateTime cancelledAt
    ) {
        public static PaymentInfo from(Payment p) {
            return new PaymentInfo(
                    p.getId(),
                    p.getOrderId(),
                    p.getAmount(),
                    p.getMethod().name(),
                    p.getStatus().name(),
                    p.getStatus().getLabel(),
                    p.getRefundedAmount(),
                    p.getCancelReason(),
                    p.getPaidAt(),
                    p.getCancelledAt()
            );
        }
    }

    public static AdminBookingDetailResponse of(Booking booking, Payment payment) {
        return new AdminBookingDetailResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getEmail(),
                booking.getUser().getName(),
                booking.getShowSchedule().getId(),
                booking.getShowSchedule().getShow().getTitle(),
                booking.getShowSchedule().getShow().getVenue(),
                booking.getShowSchedule().getScheduledAt(),
                booking.getBookingSeats().stream().map(SeatInfo::from).toList(),
                booking.getOriginalPrice(),
                booking.getDiscountPrice(),
                booking.getFinalPrice(),
                booking.getStatus().name(),
                booking.getStatus().getLabel(),
                booking.getCreatedAt(),
                booking.getPaidAt(),
                payment == null ? null : PaymentInfo.from(payment)
        );
    }
}
