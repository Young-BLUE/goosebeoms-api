package com.goosebeoms.tickets.domain.payment.service;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.payment.entity.Payment;
import com.goosebeoms.tickets.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.payment.gateway", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGateway implements PaymentService {

    public static final String MOCK_FAIL_KEY = "MOCK_FAIL";

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment prepare(Booking booking, Payment.PaymentMethod method) {
        return paymentRepository.findByBookingId(booking.getId())
                .filter(p -> p.getStatus() == Payment.PaymentStatus.PENDING)
                .orElseGet(() -> paymentRepository.save(Payment.builder()
                        .booking(booking)
                        .orderId("MOCK-" + UUID.randomUUID())
                        .amount(booking.getFinalPrice())
                        .method(method == null ? Payment.PaymentMethod.MOCK : method)
                        .build()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment confirm(Payment payment, String paymentKey, int amount) {
        Payment managed = paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new IllegalStateException("Payment not found: " + payment.getId()));
        if (MOCK_FAIL_KEY.equals(paymentKey)) {
            managed.markFailed("MOCK_FAIL", "Mock gateway forced failure");
        } else {
            managed.markSuccess(paymentKey);
        }
        return managed;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment cancel(Payment payment, String reason) {
        Payment managed = paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new IllegalStateException("Payment not found: " + payment.getId()));
        managed.markRefunded(reason, managed.getAmount());
        return managed;
    }

    @Override
    public String clientKey() {
        return "MOCK_CLIENT_KEY";
    }

    @Override
    public String variantKey() {
        return "DEFAULT";
    }
}
