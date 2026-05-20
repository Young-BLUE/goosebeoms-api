package com.goosebeoms.tickets.domain.payment.service;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.payment.dto.PaymentRequest;
import com.goosebeoms.tickets.domain.payment.entity.Payment;
import com.goosebeoms.tickets.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MockPaymentGateway implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment process(Booking booking, PaymentRequest request) {
        Payment payment = paymentRepository.save(Payment.builder()
                .booking(booking)
                .amount(booking.getFinalPrice())
                .method(request.method())
                .build());

        String txnId = "MOCK-" + UUID.randomUUID();
        if (request.mockResultOrDefault() == PaymentRequest.MockResult.SUCCESS) {
            payment.markSuccess(txnId);
        } else {
            payment.markFailed(txnId);
        }
        return payment;
    }
}
