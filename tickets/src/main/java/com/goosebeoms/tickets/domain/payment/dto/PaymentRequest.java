package com.goosebeoms.tickets.domain.payment.dto;

import com.goosebeoms.tickets.domain.payment.entity.Payment;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Payment.PaymentMethod method,
        MockResult mockResult
) {
    public enum MockResult { SUCCESS, FAIL }

    public MockResult mockResultOrDefault() {
        return mockResult == null ? MockResult.SUCCESS : mockResult;
    }
}
