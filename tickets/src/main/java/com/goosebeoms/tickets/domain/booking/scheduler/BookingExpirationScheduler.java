package com.goosebeoms.tickets.domain.booking.scheduler;

import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Scheduled(fixedDelay = 60_000L)
    public void expireHolds() {
        List<Long> expiredIds = bookingRepository.findExpiredHoldIds(LocalDateTime.now());
        if (expiredIds.isEmpty()) return;

        log.info("Expiring {} stale bookings", expiredIds.size());
        for (Long id : expiredIds) {
            try {
                bookingService.expire(id);
            } catch (Exception e) {
                log.warn("Failed to expire booking {}: {}", id, e.getMessage());
            }
        }
    }
}
