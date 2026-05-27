package com.goosebeoms.tickets.domain.booking.dto;

import com.goosebeoms.tickets.domain.booking.entity.Booking;

public record BookingCancelResponse(
        Long bookingId,
        String status,
        String statusLabel,
        CouponRestoreResult couponRestore
) {
    public record CouponRestoreResult(boolean restored, String message) {
        public static CouponRestoreResult success() {
            return new CouponRestoreResult(true, "쿠폰이 복원되었습니다.");
        }

        public static CouponRestoreResult expired() {
            return new CouponRestoreResult(false, "쿠폰 유효기간이 만료되어 복원되지 않았습니다.");
        }
    }

    public static BookingCancelResponse of(Booking booking, CouponRestoreResult couponRestore) {
        return new BookingCancelResponse(
                booking.getId(),
                booking.getStatus().name(),
                booking.getStatus().getLabel(),
                couponRestore
        );
    }
}
