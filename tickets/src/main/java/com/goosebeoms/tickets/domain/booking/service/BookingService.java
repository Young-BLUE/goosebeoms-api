package com.goosebeoms.tickets.domain.booking.service;

import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.dto.BookingResponse;
import com.goosebeoms.tickets.domain.booking.dto.BookingSummaryResponse;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.entity.BookingSeat;
import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.booking.repository.BookingSeatRepository;
import com.goosebeoms.tickets.domain.coupon.entity.UserCoupon;
import com.goosebeoms.tickets.domain.coupon.repository.UserCouponRepository;
import com.goosebeoms.tickets.domain.payment.dto.PaymentRequest;
import com.goosebeoms.tickets.domain.payment.entity.Payment;
import com.goosebeoms.tickets.domain.payment.service.PaymentService;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.repository.SeatRepository;
import com.goosebeoms.tickets.domain.show.repository.ShowScheduleRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public BookingResponse hold(String email, BookingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ShowSchedule schedule = scheduleRepository.findByIdWithOptimisticLock(request.scheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        List<Seat> seats = seatRepository.findByIdsWithPessimisticLock(request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }

        boolean hasUnavailable = seats.stream()
                .anyMatch(s -> s.getStatus() != Seat.SeatStatus.AVAILABLE);
        if (hasUnavailable) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        seats.forEach(Seat::tempReserve);
        schedule.decreaseAvailableCount(seats.size());

        int originalPrice = seats.stream().mapToInt(s -> s.getZone().getPrice()).sum();
        int discountPrice = 0;
        UserCoupon userCoupon = null;

        if (request.userCouponId() != null) {
            userCoupon = userCouponRepository.findByIdAndUserId(request.userCouponId(), user.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE));
            if (!userCoupon.isUsable(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE);
            }
            discountPrice = userCoupon.getCoupon().calculateDiscount(originalPrice);
        }

        Booking booking = bookingRepository.save(Booking.builder()
                .user(user)
                .showSchedule(schedule)
                .userCoupon(userCoupon)
                .originalPrice(originalPrice)
                .discountPrice(discountPrice)
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

    public BookingResponse pay(Long bookingId, String email, PaymentRequest request) {
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

        Payment payment = paymentService.process(booking, request);
        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }

        booking.getBookingSeats().forEach(bs -> bs.getSeat().confirm());
        if (booking.getUserCoupon() != null) {
            booking.getUserCoupon().use();
        }
        booking.confirm();

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

    public void cancel(Long bookingId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Booking booking = bookingRepository.findByIdWithSeats(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }
        if (booking.getStatus() != Booking.BookingStatus.HOLD
                && booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        boolean wasConfirmed = booking.getStatus() == Booking.BookingStatus.CONFIRMED;
        booking.getBookingSeats().forEach(bs -> bs.getSeat().release());
        booking.getShowSchedule().increaseAvailableCount(booking.getBookingSeats().size());

        if (wasConfirmed && booking.getUserCoupon() != null) {
            booking.getUserCoupon().restore();
        }
        booking.cancel();
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
    }

    private void releaseHold(Booking booking) {
        booking.getBookingSeats().forEach(bs -> bs.getSeat().release());
        booking.getShowSchedule().increaseAvailableCount(booking.getBookingSeats().size());
    }
}
