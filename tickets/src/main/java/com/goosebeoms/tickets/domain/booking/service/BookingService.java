package com.goosebeoms.tickets.domain.booking.service;

import com.goosebeoms.tickets.domain.booking.dto.BookingCancelResponse;
import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.dto.BookingResponse;
import com.goosebeoms.tickets.domain.booking.dto.BookingSummaryResponse;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.entity.BookingSeat;
import com.goosebeoms.tickets.domain.booking.event.BookingNotificationEvent;
import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.booking.repository.BookingSeatRepository;
import com.goosebeoms.tickets.domain.coupon.entity.UserCoupon;
import com.goosebeoms.tickets.domain.coupon.repository.UserCouponRepository;
import com.goosebeoms.tickets.domain.payment.dto.PaymentConfirmRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareResponse;
import com.goosebeoms.tickets.domain.payment.entity.Payment;
import com.goosebeoms.tickets.domain.payment.repository.PaymentRepository;
import com.goosebeoms.tickets.domain.payment.service.PaymentService;
import com.goosebeoms.tickets.domain.notification.entity.Notification;
import com.goosebeoms.tickets.domain.queue.service.QueueTokenService;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.event.SeatStatusChangedEvent;
import com.goosebeoms.tickets.domain.show.repository.SeatRepository;
import com.goosebeoms.tickets.domain.show.repository.ShowScheduleRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final ObjectProvider<QueueTokenService> queueTokenServiceProvider;
    private final ApplicationEventPublisher eventPublisher;

    public BookingResponse hold(String email, BookingRequest request, String queueToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        requireValidQueueToken(queueToken, request.scheduleId(), user.getId());

        ShowSchedule schedule = scheduleRepository.findByIdWithOptimisticLock(request.scheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        List<Seat> seats = reserveSeats(request.seatIds(), schedule);
        Pricing pricing = calculatePricing(seats, request.userCouponId(), user.getId());

        return persistBooking(user, schedule, seats, pricing);
    }

    private void requireValidQueueToken(String queueToken, Long scheduleId, Long userId) {
        QueueTokenService queueTokenService = queueTokenServiceProvider.getIfAvailable();
        if (queueTokenService != null) {
            queueTokenService.requireValid(queueToken, scheduleId, userId);
        }
    }

    private List<Seat> reserveSeats(List<Long> seatIds, ShowSchedule schedule) {
        List<Seat> seats = seatRepository.findByIdsWithPessimisticLock(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        boolean hasUnavailable = seats.stream()
                .anyMatch(s -> s.getStatus() != Seat.SeatStatus.AVAILABLE);
        if (hasUnavailable) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        seats.forEach(Seat::tempReserve);
        schedule.decreaseAvailableCount(seats.size());
        publishSeatChanges(schedule.getId(), seats);
        return seats;
    }

    private Pricing calculatePricing(List<Seat> seats, Long userCouponId, Long userId) {
        int originalPrice = seats.stream().mapToInt(s -> s.getZone().getPrice()).sum();
        if (userCouponId == null) {
            return new Pricing(originalPrice, 0, null);
        }
        UserCoupon userCoupon = userCouponRepository.findByIdAndUserId(userCouponId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE));
        if (!userCoupon.isUsable(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE);
        }
        int discountPrice = userCoupon.getCoupon().calculateDiscount(originalPrice);
        return new Pricing(originalPrice, discountPrice, userCoupon);
    }

    private BookingResponse persistBooking(User user, ShowSchedule schedule, List<Seat> seats, Pricing pricing) {
        Booking booking = bookingRepository.save(Booking.builder()
                .user(user)
                .showSchedule(schedule)
                .userCoupon(pricing.userCoupon())
                .originalPrice(pricing.originalPrice())
                .discountPrice(pricing.discountPrice())
                .build());

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> BookingSeat.builder()
                        .booking(booking)
                        .seat(seat)
                        .price(seat.getZone().getPrice())
                        .build())
                .toList();
        bookingSeatRepository.saveAll(bookingSeats);

        return BookingResponse.from(booking, bookingSeats);
    }

    private record Pricing(int originalPrice, int discountPrice, UserCoupon userCoupon) {}

    public PaymentPrepareResponse preparePayment(Long bookingId, String email, PaymentPrepareRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Booking booking = bookingRepository.findByIdWithSeats(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }
        if (booking.getStatus() != Booking.BookingStatus.HOLD) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_PAYABLE);
        }
        if (booking.isHoldExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BOOKING_HOLD_EXPIRED);
        }

        Payment payment = paymentService.prepare(booking, request.methodOrDefault());
        return PaymentPrepareResponse.of(payment, user.getEmail(), user.getName(),
                paymentService.clientKey(), paymentService.variantKey());
    }

    public BookingResponse confirmPayment(Long bookingId, String email, PaymentConfirmRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Booking booking = bookingRepository.findByIdWithSeats(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getOrderId().equals(request.orderId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ORDER_MISMATCH);
        }
        if (payment.getAmount() != request.amount()) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED
                && payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return BookingResponse.from(booking, booking.getBookingSeats());
        }
        if (booking.getStatus() != Booking.BookingStatus.HOLD) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_PAYABLE);
        }
        if (booking.isHoldExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BOOKING_HOLD_EXPIRED);
        }

        Payment confirmed = (payment.getStatus() == Payment.PaymentStatus.SUCCESS)
                ? payment
                : paymentService.confirm(payment, request.paymentKey(), request.amount());
        if (confirmed.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }

        booking.getBookingSeats().forEach(bs -> bs.getSeat().confirm());
        publishSeatChangesFromBookingSeats(booking.getShowSchedule().getId(), booking.getBookingSeats());
        if (booking.getUserCoupon() != null) {
            booking.getUserCoupon().use();
        }
        booking.confirm();

        eventPublisher.publishEvent(new BookingNotificationEvent(
                user.getId(),
                Notification.Type.BOOKING_CONFIRMED,
                "예매가 완료되었습니다",
                "예매 #" + booking.getId() + " 결제가 완료되었습니다. 좌석을 확정했습니다.",
                booking.getId()));

        return BookingResponse.from(booking, booking.getBookingSeats());
    }

    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> getMyBookings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(BookingSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Booking booking = bookingRepository.findByIdWithSeats(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }

        return BookingResponse.from(booking, booking.getBookingSeats());
    }

    public BookingCancelResponse cancel(Long bookingId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Booking booking = bookingRepository.findByIdWithSeats(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }
        return cancelInternal(booking, "사용자 요청 취소", false);
    }

    public BookingCancelResponse forceCancel(Long bookingId, String adminReason) {
        Booking booking = bookingRepository.findByIdWithSeats(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        String reason = (adminReason == null || adminReason.isBlank()) ? "관리자 강제 취소" : adminReason;
        return cancelInternal(booking, reason, true);
    }

    private BookingCancelResponse cancelInternal(Booking booking, String reason, boolean byAdmin) {
        if (booking.getStatus() != Booking.BookingStatus.HOLD
                && booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        boolean wasConfirmed = booking.getStatus() == Booking.BookingStatus.CONFIRMED;

        BookingCancelResponse.PaymentRefundResult refundResult =
                BookingCancelResponse.PaymentRefundResult.notApplicable();
        if (wasConfirmed) {
            Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
            if (payment != null && payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                Payment refunded = paymentService.cancel(payment, reason);
                refundResult = BookingCancelResponse.PaymentRefundResult.success(refunded.getRefundedAmount());
            }
        }

        booking.getBookingSeats().forEach(bs -> bs.getSeat().release());
        booking.getShowSchedule().increaseAvailableCount(booking.getBookingSeats().size());
        publishSeatChangesFromBookingSeats(booking.getShowSchedule().getId(), booking.getBookingSeats());

        BookingCancelResponse.CouponRestoreResult couponRestore = null;
        if (wasConfirmed && booking.getUserCoupon() != null) {
            UserCoupon userCoupon = booking.getUserCoupon();
            if (LocalDateTime.now().isBefore(userCoupon.getCoupon().getValidUntil())) {
                userCoupon.restore();
                couponRestore = BookingCancelResponse.CouponRestoreResult.success();
            } else {
                couponRestore = BookingCancelResponse.CouponRestoreResult.expired();
            }
        }
        booking.cancel();

        String title = byAdmin ? "관리자에 의해 예매가 취소되었습니다" : "예매가 취소되었습니다";
        StringBuilder msg = new StringBuilder("예매 #" + booking.getId() + "을(를) 취소했습니다.");
        if (byAdmin) msg.append(" 사유: ").append(reason).append(".");
        if (refundResult.refunded()) msg.append(" ").append(refundResult.message());
        if (couponRestore != null) msg.append(" ").append(couponRestore.message());

        eventPublisher.publishEvent(new BookingNotificationEvent(
                booking.getUser().getId(),
                Notification.Type.BOOKING_CANCELLED,
                title,
                msg.toString(),
                booking.getId()));

        return BookingCancelResponse.of(booking, couponRestore, refundResult);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expire(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithSeats(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != Booking.BookingStatus.HOLD) {
            return;
        }
        releaseHold(booking);
        booking.expire();

        eventPublisher.publishEvent(new BookingNotificationEvent(
                booking.getUser().getId(),
                Notification.Type.BOOKING_EXPIRED,
                "좌석 점유가 만료되었습니다",
                "예매 #" + booking.getId() + "의 결제 시간이 지나 좌석이 해제되었습니다.",
                booking.getId()));
    }

    private void releaseHold(Booking booking) {
        booking.getBookingSeats().forEach(bs -> bs.getSeat().release());
        booking.getShowSchedule().increaseAvailableCount(booking.getBookingSeats().size());
        publishSeatChangesFromBookingSeats(booking.getShowSchedule().getId(), booking.getBookingSeats());
    }

    private void publishSeatChanges(Long scheduleId, List<Seat> seats) {
        List<SeatStatusChangedEvent.SeatChange> changes = seats.stream()
                .map(SeatStatusChangedEvent.SeatChange::from)
                .toList();
        eventPublisher.publishEvent(new SeatStatusChangedEvent(scheduleId, changes));
    }

    private void publishSeatChangesFromBookingSeats(Long scheduleId, Collection<BookingSeat> bookingSeats) {
        List<Seat> seats = bookingSeats.stream().map(BookingSeat::getSeat).toList();
        publishSeatChanges(scheduleId, seats);
    }
}
