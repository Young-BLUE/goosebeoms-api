package com.goosebeoms.tickets.domain.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossConfirmResponse(
        String paymentKey,
        String orderId,
        String status,
        int totalAmount,
        String method,
        String approvedAt
) {}
