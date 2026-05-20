package com.goosebeoms.tickets.domain.payment.repository;

import com.goosebeoms.tickets.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByOrderId(String orderId);
}
