package com.goosebeoms.tickets.domain.booking.service;

import com.goosebeoms.tickets.domain.booking.dto.AdminBookingResponse;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
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

    public Page<AdminBookingResponse> search(Long userId, Long scheduleId, Booking.BookingStatus status,
                                             LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return bookingRepository.searchForAdmin(userId, scheduleId, status, from, to, pageable)
                .map(AdminBookingResponse::from);
    }
}
