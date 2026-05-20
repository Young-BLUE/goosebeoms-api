package com.goosebeoms.tickets.domain.payment;

import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.dto.BookingResponse;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import com.goosebeoms.tickets.domain.payment.client.TossApiException;
import com.goosebeoms.tickets.domain.payment.client.TossConfirmResponse;
import com.goosebeoms.tickets.domain.payment.client.TossPaymentClient;
import com.goosebeoms.tickets.domain.payment.dto.PaymentConfirmRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareResponse;
import com.goosebeoms.tickets.domain.payment.entity.Payment;
import com.goosebeoms.tickets.domain.payment.repository.PaymentRepository;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.repository.SeatRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = {
        "app.payment.gateway=toss",
        "app.payment.toss.client-key=test_ck_dummy",
        "app.payment.toss.secret-key=test_sk_dummy",
        "app.payment.toss.base-url=http://localhost",
        "app.payment.toss.connect-timeout-ms=3000",
        "app.payment.toss.read-timeout-ms=10000"
})
class TossPaymentGatewayTest extends AbstractIntegrationTest {

    @MockitoBean TossPaymentClient tossClient;
    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired TestDataFactory factory;

    @BeforeEach
    void clean() {
        factory.flushRedis();
    }

    @Test
    void successfulTossConfirmTransitionsBookingToConfirmed() {
        Fixture fx = setupHeldBooking("toss-ok");
        PaymentPrepareResponse prepared = bookingService.preparePayment(fx.bookingId, fx.email,
                new PaymentPrepareRequest(Payment.PaymentMethod.CARD));

        when(tossClient.confirm(anyString(), eq(prepared.orderId()), eq(prepared.amount())))
                .thenReturn(new TossConfirmResponse("tossPaymentKey-1", prepared.orderId(),
                        "DONE", prepared.amount(), "카드", "2026-05-20T15:00:00"));

        BookingResponse confirmed = bookingService.confirmPayment(fx.bookingId, fx.email,
                new PaymentConfirmRequest("tossPaymentKey-1", prepared.orderId(), prepared.amount()));

        assertThat(confirmed.status()).isEqualTo(Booking.BookingStatus.CONFIRMED.name());
        Payment payment = paymentRepository.findByBookingId(fx.bookingId).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.SUCCESS);
        assertThat(payment.getProviderTxnId()).isEqualTo("tossPaymentKey-1");
        assertThat(seatRepository.findById(fx.seatId).orElseThrow().getStatus())
                .isEqualTo(Seat.SeatStatus.SOLD);
    }

    @Test
    void failedTossConfirmKeepsBookingInHold() {
        Fixture fx = setupHeldBooking("toss-fail");
        PaymentPrepareResponse prepared = bookingService.preparePayment(fx.bookingId, fx.email,
                new PaymentPrepareRequest(Payment.PaymentMethod.CARD));

        when(tossClient.confirm(anyString(), anyString(), anyInt()))
                .thenThrow(new TossApiException("INVALID_CARD", "유효하지 않은 카드입니다"));

        assertThatThrownBy(() -> bookingService.confirmPayment(fx.bookingId, fx.email,
                new PaymentConfirmRequest("badKey", prepared.orderId(), prepared.amount())))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PAYMENT_FAILED);

        Payment payment = paymentRepository.findByBookingId(fx.bookingId).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
        assertThat(payment.getFailureCode()).isEqualTo("INVALID_CARD");
        assertThat(bookingRepository.findById(fx.bookingId).orElseThrow().getStatus())
                .isEqualTo(Booking.BookingStatus.HOLD);
    }

    @Test
    void amountMismatchIsBlockedBeforeCallingToss() {
        Fixture fx = setupHeldBooking("toss-amt");
        PaymentPrepareResponse prepared = bookingService.preparePayment(fx.bookingId, fx.email,
                new PaymentPrepareRequest(Payment.PaymentMethod.CARD));

        assertThatThrownBy(() -> bookingService.confirmPayment(fx.bookingId, fx.email,
                new PaymentConfirmRequest("anyKey", prepared.orderId(), prepared.amount() + 1)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PAYMENT_AMOUNT_MISMATCH);

        verify(tossClient, times(0)).confirm(anyString(), anyString(), anyInt());
    }

    @Test
    void confirmIsIdempotentWhenAlreadySuccess() {
        Fixture fx = setupHeldBooking("toss-idem");
        PaymentPrepareResponse prepared = bookingService.preparePayment(fx.bookingId, fx.email,
                new PaymentPrepareRequest(Payment.PaymentMethod.CARD));

        when(tossClient.confirm(anyString(), eq(prepared.orderId()), eq(prepared.amount())))
                .thenReturn(new TossConfirmResponse("tossKey", prepared.orderId(),
                        "DONE", prepared.amount(), "카드", "2026-05-20T15:00:00"));

        bookingService.confirmPayment(fx.bookingId, fx.email,
                new PaymentConfirmRequest("tossKey", prepared.orderId(), prepared.amount()));
        BookingResponse second = bookingService.confirmPayment(fx.bookingId, fx.email,
                new PaymentConfirmRequest("tossKey", prepared.orderId(), prepared.amount()));

        assertThat(second.status()).isEqualTo(Booking.BookingStatus.CONFIRMED.name());
        verify(tossClient, times(1)).confirm(anyString(), anyString(), anyInt());
    }

    private Fixture setupHeldBooking(String prefix) {
        ShowSchedule schedule = factory.newSchedule(2);
        List<Seat> seats = factory.seatsOf(schedule);
        User user = factory.newUser(prefix + "@test.com");
        String queueToken = factory.issueQueueToken(schedule.getId(), user.getId());

        BookingResponse held = bookingService.hold(user.getEmail(),
                new BookingRequest(schedule.getId(), List.of(seats.get(0).getId()), null),
                queueToken);
        return new Fixture(held.id(), user.getEmail(), seats.get(0).getId());
    }

    private record Fixture(Long bookingId, String email, Long seatId) {}
}
