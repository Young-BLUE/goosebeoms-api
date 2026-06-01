package com.goosebeoms.tickets.domain.admin.service;

import com.goosebeoms.tickets.domain.admin.dto.AdminStatsResponse;
import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.coupon.repository.CouponRepository;
import com.goosebeoms.tickets.domain.show.entity.Show;
import com.goosebeoms.tickets.domain.show.repository.ShowRepository;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    public AdminStatsResponse get() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        return new AdminStatsResponse(
                bookingRepository.count(),
                bookingRepository.countByCreatedAtBetween(todayStart, todayEnd),
                bookingRepository.sumConfirmedRevenue(),
                bookingRepository.sumConfirmedRevenueBetween(todayStart, todayEnd),
                showRepository.countByStatus(Show.Status.ON_SALE),
                showRepository.countByStatus(Show.Status.SOLD_OUT),
                userRepository.count(),
                couponRepository.countByValidUntilAfter(now)
        );
    }
}
