package com.goosebeoms.tickets.domain.payment.dto;

import com.goosebeoms.tickets.domain.payment.entity.Payment;

public record PaymentPrepareResponse(
        Long paymentId,
        String orderId,
        int amount,
        String customerEmail,
        String customerName,
        String clientKey,
        String variantKey,
        String method,
        String methodLabel
) {
    public static PaymentPrepareResponse of(Payment payment, String customerEmail, String customerName,
                                            String clientKey, String variantKey) {
        return new PaymentPrepareResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                customerEmail,
                customerName,
                clientKey,
                variantKey,
                payment.getMethod().name(),
                payment.getMethod().getLabel()
        );
    }
}
