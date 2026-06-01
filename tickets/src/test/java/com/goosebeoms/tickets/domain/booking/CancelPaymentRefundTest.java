package com.goosebeoms.tickets.domain.booking;

import com.goosebeoms.tickets.domain.booking.dto.BookingCancelResponse;
import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.dto.BookingResponse;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import com.goosebeoms.tickets.domain.payment.dto.PaymentConfirmRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareResponse;
import com.goosebeoms.tickets.domain.payment.entity.Payment;
import com.goosebeoms.tickets.domain.payment.repository.PaymentRepository;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CancelPaymentRefundTest extends AbstractIntegrationTest {

    @Autowired BookingService bookingService;
    @Autowired PaymentRepository paymentRepository;
    @Autowired TestDataFactory factory;

    @Test
    void cancellingConfirmedBookingRefundsPayment() {
        ShowSchedule schedule = factory.newSchedule(2);
        List<Seat> seats = factory.seatsOf(schedule);
        User user = factory.newUser("payee@test.com");
        String queueToken = factory.issueQueueToken(schedule.getId(), user.getId());

        BookingResponse held = bookingService.hold(user.getEmail(),
                new BookingRequest(schedule.getId(), List.of(seats.get(0).getId()), null),
                queueToken);

        PaymentPrepareResponse prepared = bookingService.preparePayment(held.id(), user.getEmail(),
                new PaymentPrepareRequest(Payment.PaymentMethod.MOCK));
        bookingService.confirmPayment(held.id(), user.getEmail(),
                new PaymentConfirmRequest("MOCK_OK", prepared.orderId(), prepared.amount()));

        Payment payment = paymentRepository.findByBookingId(held.id()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.SUCCESS);
        int paidAmount = payment.getAmount();

        BookingCancelResponse response = bookingService.cancel(held.id(), user.getEmail());

        assertThat(response.paymentRefund().refunded()).isTrue();
        assertThat(response.paymentRefund().refundedAmount()).isEqualTo(paidAmount);

        Payment refunded = paymentRepository.findByBookingId(held.id()).orElseThrow();
        assertThat(refunded.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);
        assertThat(refunded.getRefundedAmount()).isEqualTo(paidAmount);
        assertThat(refunded.getCancelledAt()).isNotNull();
        assertThat(refunded.getCancelReason()).isEqualTo("사용자 요청 취소");
    }

    @Test
    void cancellingHeldBookingDoesNotAttemptRefund() {
        ShowSchedule schedule = factory.newSchedule(2);
        List<Seat> seats = factory.seatsOf(schedule);
        User user = factory.newUser("holdee@test.com");
        String queueToken = factory.issueQueueToken(schedule.getId(), user.getId());

        BookingResponse held = bookingService.hold(user.getEmail(),
                new BookingRequest(schedule.getId(), List.of(seats.get(0).getId()), null),
                queueToken);

        BookingCancelResponse response = bookingService.cancel(held.id(), user.getEmail());

        assertThat(response.paymentRefund().refunded()).isFalse();
        assertThat(response.paymentRefund().refundedAmount()).isZero();
    }
}
