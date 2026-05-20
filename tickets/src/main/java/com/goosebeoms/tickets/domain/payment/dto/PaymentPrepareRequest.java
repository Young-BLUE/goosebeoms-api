package com.goosebeoms.tickets.domain.payment.dto;

import com.goosebeoms.tickets.domain.payment.entity.Payment;

public record PaymentPrepareRequest(Payment.PaymentMethod method) {
    public Payment.PaymentMethod methodOrDefault() {
        return method == null ? Payment.PaymentMethod.CARD : method;
    }
}
