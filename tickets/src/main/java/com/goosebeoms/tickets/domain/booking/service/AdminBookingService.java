package com.goosebeoms.tickets.domain.booking.service;

import com.goosebeoms.tickets.domain.booking.dto.AdminBookingDetailResponse;
import com.goosebeoms.tickets.domain.booking.dto.AdminBookingResponse;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.payment.entity.Payment;
import com.goosebeoms.tickets.domain.payment.repository.PaymentRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public Page<AdminBookingResponse> search(Long userId, Long scheduleId, Booking.BookingStatus status,
                                             LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return bookingRepository.searchForAdmin(userId, scheduleId, status, from, to, pageable)
                .map(AdminBookingResponse::from);
    }

    public AdminBookingDetailResponse get(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithSeats(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        Payment payment = paymentRepository.findByBookingId(bookingId).orElse(null);
        return AdminBookingDetailResponse.of(booking, payment);
    }
}
