package com.goosebeoms.tickets.domain.payment.service;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.payment.client.TossApiException;
import com.goosebeoms.tickets.domain.payment.client.TossConfirmResponse;
import com.goosebeoms.tickets.domain.payment.client.TossPaymentClient;
import com.goosebeoms.tickets.domain.payment.entity.Payment;
import com.goosebeoms.tickets.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.payment.gateway", havingValue = "toss")
public class TossPaymentGateway implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossClient;

    @Value("${app.payment.toss.client-key}")
    private String clientKey;

    @Value("${app.payment.toss.variant-key:DEFAULT}")
    private String variantKey;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment prepare(Booking booking, Payment.PaymentMethod method) {
        return paymentRepository.findByBookingId(booking.getId())
                .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING)
                .orElseGet(() -> paymentRepository.save(Payment.builder()
                        .booking(booking)
                        .orderId("ORDER-" + booking.getId() + "-" + UUID.randomUUID())
                        .amount(booking.getFinalPrice())
                        .method(method == null ? Payment.PaymentMethod.CARD : method)
                        .build()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment confirm(Payment payment, String paymentKey, int amount) {
        Payment managed = paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new IllegalStateException("Payment not found: " + payment.getId()));
        try {
            TossConfirmResponse response = tossClient.confirm(paymentKey, managed.getOrderId(), amount);
            managed.markSuccess(response.paymentKey());
        } catch (TossApiException e) {
            managed.markFailed(e.getCode(), e.getMessage());
        }
        return managed;
    }

    @Override
    public String clientKey() {
        return clientKey;
    }

    @Override
    public String variantKey() {
        return variantKey;
    }
}
