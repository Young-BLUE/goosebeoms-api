package com.goosebeoms.tickets.domain.payment.service;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.payment.client.TossApiException;
import com.goosebeoms.tickets.domain.payment.client.TossCancelResponse;
import com.goosebeoms.tickets.domain.payment.client.TossConfirmResponse;
import com.goosebeoms.tickets.domain.payment.client.TossPaymentClient;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment cancel(Payment payment, String reason) {
        Payment managed = paymentRepository.findById(payment.getId())
                .orElseThrow(() -> new IllegalStateException("Payment not found: " + payment.getId()));
        if (managed.getStatus() == Payment.PaymentStatus.REFUNDED) {
            return managed;
        }
        if (managed.getProviderTxnId() == null) {
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }
        try {
            TossCancelResponse response = tossClient.cancel(managed.getProviderTxnId(), reason);
            int totalCancelled = response.cancels() == null ? managed.getAmount()
                    : response.cancels().stream().mapToInt(TossCancelResponse.Cancel::cancelAmount).sum();
            managed.markRefunded(reason, totalCancelled);
            return managed;
        } catch (TossApiException e) {
            throw new BusinessException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }
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
