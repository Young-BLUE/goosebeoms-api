package com.goosebeoms.tickets.domain.booking;

import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.dto.BookingResponse;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import com.goosebeoms.tickets.domain.coupon.entity.Coupon;
import com.goosebeoms.tickets.domain.coupon.entity.UserCoupon;
import com.goosebeoms.tickets.domain.coupon.repository.UserCouponRepository;
import com.goosebeoms.tickets.domain.coupon.service.CouponService;
import com.goosebeoms.tickets.domain.payment.dto.PaymentConfirmRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareResponse;
import com.goosebeoms.tickets.domain.payment.entity.Payment;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.repository.SeatRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CancelCouponRefundTest extends AbstractIntegrationTest {

    @Autowired BookingService bookingService;
    @Autowired CouponService couponService;
    @Autowired BookingRepository bookingRepository;
    @Autowired UserCouponRepository userCouponRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired TestDataFactory factory;

    @Test
    void cancellingConfirmedBookingRestoresCoupon() {
        ShowSchedule schedule = factory.newSchedule(2);
        List<Seat> seats = factory.seatsOf(schedule);
        User user = factory.newUser("refundee@test.com");
        Coupon coupon = factory.newCoupon("REFUND10", 10);

        var issued = couponService.issue(coupon.getId(), user.getEmail());
        String queueToken = factory.issueQueueToken(schedule.getId(), user.getId());

        BookingResponse held = bookingService.hold(user.getEmail(),
                new BookingRequest(schedule.getId(), List.of(seats.get(0).getId()), issued.id()),
                queueToken);

        PaymentPrepareResponse prepared = bookingService.preparePayment(held.id(), user.getEmail(),
                new PaymentPrepareRequest(Payment.PaymentMethod.MOCK));
        bookingService.confirmPayment(held.id(), user.getEmail(),
                new PaymentConfirmRequest("MOCK_OK", prepared.orderId(), prepared.amount()));

        assertThat(userCouponRepository.findById(issued.id()).orElseThrow().getStatus())
                .isEqualTo(UserCoupon.Status.USED);
        assertThat(bookingRepository.findById(held.id()).orElseThrow().getStatus())
                .isEqualTo(Booking.BookingStatus.CONFIRMED);

        bookingService.cancel(held.id(), user.getEmail());

        UserCoupon restored = userCouponRepository.findById(issued.id()).orElseThrow();
        assertThat(restored.getStatus()).isEqualTo(UserCoupon.Status.AVAILABLE);
        assertThat(restored.getUsedAt()).isNull();
        assertThat(seatRepository.findById(seats.get(0).getId()).orElseThrow().getStatus())
                .isEqualTo(Seat.SeatStatus.AVAILABLE);
        assertThat(bookingRepository.findById(held.id()).orElseThrow().getStatus())
                .isEqualTo(Booking.BookingStatus.CANCELLED);
    }
}
