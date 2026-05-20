package com.goosebeoms.tickets.domain.booking;

import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.dto.BookingResponse;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.booking.scheduler.BookingExpirationScheduler;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.repository.SeatRepository;
import com.goosebeoms.tickets.domain.show.repository.ShowScheduleRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingExpirationTest extends AbstractIntegrationTest {

    @Autowired BookingService bookingService;
    @Autowired BookingExpirationScheduler scheduler;
    @Autowired BookingRepository bookingRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired ShowScheduleRepository scheduleRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired TestDataFactory factory;

    @Test
    void schedulerReleasesExpiredHolds() {
        ShowSchedule schedule = factory.newSchedule(2);
        List<Seat> seats = factory.seatsOf(schedule);
        User user = factory.newUser("expirer@test.com");
        int beforeAvailable = scheduleRepository.findById(schedule.getId()).orElseThrow().getAvailableCount();

        BookingResponse held = bookingService.hold(user.getEmail(),
                new BookingRequest(schedule.getId(), List.of(seats.get(0).getId()), null));

        jdbc.update("UPDATE bookings SET hold_expires_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusHours(1)), held.id());

        scheduler.expireHolds();

        Booking reloaded = bookingRepository.findById(held.id()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(Booking.BookingStatus.EXPIRED);
        assertThat(seatRepository.findById(seats.get(0).getId()).orElseThrow().getStatus())
                .isEqualTo(Seat.SeatStatus.AVAILABLE);
        assertThat(scheduleRepository.findById(schedule.getId()).orElseThrow().getAvailableCount())
                .isEqualTo(beforeAvailable);
    }
}
