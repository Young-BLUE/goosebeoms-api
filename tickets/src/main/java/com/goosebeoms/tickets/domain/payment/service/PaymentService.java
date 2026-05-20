package com.goosebeoms.tickets.domain.payment.service;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.payment.dto.PaymentRequest;
import com.goosebeoms.tickets.domain.payment.entity.Payment;

public interface PaymentService {
    Payment process(Booking booking, PaymentRequest request);
}
