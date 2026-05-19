package com.goosebeoms.tickets.domain.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookingRequest(
        @NotNull Long scheduleId,
        @NotEmpty List<Long> seatIds,
        Long userCouponId
) {}
