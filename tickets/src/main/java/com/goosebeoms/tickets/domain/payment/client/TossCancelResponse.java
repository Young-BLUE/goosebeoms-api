package com.goosebeoms.tickets.domain.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossCancelResponse(
        String paymentKey,
        String orderId,
        String status,
        int totalAmount,
        int balanceAmount,
        List<Cancel> cancels
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cancel(
            int cancelAmount,
            String cancelReason,
            String canceledAt
    ) {}
}
