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
import org.springframework.transaction.annotation.Transactional;

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

    public BookingResponse book(String email, BookingRequest request) {
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

        int originalPrice = seats.stream().mapToInt(s -> s.getZone().getPrice()).sum();
        int discountPrice = 0;
        UserCoupon userCoupon = null;

        if (request.userCouponId() != null) {
            userCoupon = userCouponRepository.findByIdAndUserId(request.userCouponId(), user.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE));
            discountPrice = userCoupon.getCoupon().calculateDiscount(originalPrice);
            userCoupon.use();
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

        seats.forEach(Seat::confirm);
        schedule.decreaseAvailableCount(seats.size());
        booking.confirm();

        return BookingResponse.from(booking, bookingSeats);
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

        booking.cancel();
        booking.getBookingSeats().forEach(bs -> bs.getSeat().release());
        booking.getShowSchedule().increaseAvailableCount(booking.getBookingSeats().size());
    }
}
