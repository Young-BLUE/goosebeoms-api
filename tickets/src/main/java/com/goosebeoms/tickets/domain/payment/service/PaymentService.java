package com.goosebeoms.tickets.domain.payment.service;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.payment.entity.Payment;

public interface PaymentService {
    Payment prepare(Booking booking, Payment.PaymentMethod method);
    Payment confirm(Payment payment, String paymentKey, int amount);
    String clientKey();
    String variantKey();
}
