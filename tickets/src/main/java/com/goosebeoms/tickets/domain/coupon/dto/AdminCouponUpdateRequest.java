package com.goosebeoms.tickets.domain.coupon.dto;

import java.time.LocalDateTime;

public record AdminCouponUpdateRequest(
        String name,
        Integer maxCount,
        LocalDateTime validUntil
) {}
