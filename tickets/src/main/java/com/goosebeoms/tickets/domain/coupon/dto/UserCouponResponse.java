package com.goosebeoms.tickets.domain.coupon.dto;

import com.goosebeoms.tickets.domain.coupon.entity.UserCoupon;

import java.time.LocalDateTime;

public record UserCouponResponse(
        Long id,
        Long couponId,
        String name,
        String discountType,
        int discountValue,
        String status,
        LocalDateTime issuedAt
) {
    public static UserCouponResponse from(UserCoupon userCoupon) {
        return new UserCouponResponse(
                userCoupon.getId(),
                userCoupon.getCoupon().getId(),
                userCoupon.getCoupon().getName(),
                userCoupon.getCoupon().getDiscountType().name(),
                userCoupon.getCoupon().getDiscountValue(),
                userCoupon.getStatus().name(),
                userCoupon.getIssuedAt()
        );
    }
}
