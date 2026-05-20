package com.goosebeoms.tickets.domain.booking;

import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.repository.BookingRepository;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.repository.SeatRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class BookingConcurrencyTest extends AbstractIntegrationTest {

    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired TestDataFactory factory;

    @Test
    void onlyOneUserCanHoldTheSameSeat() throws InterruptedException {
        ShowSchedule schedule = factory.newSchedule(1);
        List<Seat> seats = factory.seatsOf(schedule);
        Long seatId = seats.get(0).getId();

        int threads = 200;
        List<User> users = factory.newUsers(threads, "racer");
        BookingRequest request = new BookingRequest(schedule.getId(), List.of(seatId), null);

        java.util.Map<String, String> tokens = new java.util.HashMap<>();
        for (User u : users) tokens.put(u.getEmail(), factory.issueQueueToken(schedule.getId(), u.getId()));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(32);

        for (int i = 0; i < threads; i++) {
            String email = users.get(i).getEmail();
            String token = tokens.get(email);
            pool.submit(() -> {
                try {
                    start.await();
                    bookingService.hold(email, request, token);
                    success.incrementAndGet();
                } catch (Exception e) {
                    failure.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        assertThat(success.get()).isEqualTo(1);
        assertThat(failure.get()).isEqualTo(threads - 1);
        assertThat(seatRepository.findById(seatId).orElseThrow().getStatus())
                .isEqualTo(Seat.SeatStatus.TEMP_RESERVED);
        assertThat(bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.HOLD)
                .count()).isEqualTo(1);
    }
}
