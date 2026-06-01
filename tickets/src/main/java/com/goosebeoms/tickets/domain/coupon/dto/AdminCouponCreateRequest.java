package com.goosebeoms.tickets.domain.coupon.dto;

import com.goosebeoms.tickets.domain.coupon.entity.Coupon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record AdminCouponCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull Coupon.DiscountType discountType,
        @Positive int discountValue,
        @Positive int maxCount,
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validUntil
) {}
