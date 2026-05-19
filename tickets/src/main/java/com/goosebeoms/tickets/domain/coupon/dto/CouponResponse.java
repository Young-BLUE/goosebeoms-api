package com.goosebeoms.tickets.domain.coupon.dto;

import com.goosebeoms.tickets.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String name,
        String discountType,
        int discountValue,
        int maxCount,
        int issuedCount,
        int remainingCount,
        LocalDateTime validFrom,
        LocalDateTime validUntil
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                coupon.getMaxCount(),
                coupon.getIssuedCount(),
                coupon.getMaxCount() - coupon.getIssuedCount(),
                coupon.getValidFrom(),
                coupon.getValidUntil()
        );
    }
}
