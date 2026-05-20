package com.goosebeoms.tickets.domain.booking;

import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import com.goosebeoms.tickets.domain.show.entity.Seat;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.repository.SeatRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import com.goosebeoms.tickets.support.AbstractIntegrationTest;
import com.goosebeoms.tickets.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingRequiresQueueTokenTest extends AbstractIntegrationTest {

    @Autowired BookingService bookingService;
    @Autowired SeatRepository seatRepository;
    @Autowired TestDataFactory factory;

    @BeforeEach
    void clean() {
        factory.flushRedis();
    }

    @Test
    void holdWithoutTokenIsRejected() {
        ShowSchedule schedule = factory.newSchedule(2);
        List<Seat> seats = factory.seatsOf(schedule);
        User user = factory.newUser("notoken@test.com");

        BookingRequest request = new BookingRequest(schedule.getId(), List.of(seats.get(0).getId()), null);

        assertThatThrownBy(() -> bookingService.hold(user.getEmail(), request, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.QUEUE_TOKEN_REQUIRED);
    }

    @Test
    void holdWithMismatchedScheduleTokenIsRejected() {
        ShowSchedule scheduleA = factory.newSchedule(2);
        ShowSchedule scheduleB = factory.newSchedule(2);
        List<Seat> seatsA = factory.seatsOf(scheduleA);
        User user = factory.newUser("wrong@test.com");

        String otherScheduleToken = factory.issueQueueToken(scheduleB.getId(), user.getId());

        BookingRequest request = new BookingRequest(scheduleA.getId(), List.of(seatsA.get(0).getId()), null);

        assertThatThrownBy(() -> bookingService.hold(user.getEmail(), request, otherScheduleToken))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.QUEUE_TOKEN_MISMATCH);
    }

    @Test
    void holdWithValidTokenSucceeds() {
        ShowSchedule schedule = factory.newSchedule(2);
        List<Seat> seats = factory.seatsOf(schedule);
        User user = factory.newUser("ok@test.com");
        String token = factory.issueQueueToken(schedule.getId(), user.getId());

        BookingRequest request = new BookingRequest(schedule.getId(), List.of(seats.get(0).getId()), null);
        var response = bookingService.hold(user.getEmail(), request, token);

        assertThat(response.status()).isEqualTo(Booking.BookingStatus.HOLD.name());
        assertThat(seatRepository.findById(seats.get(0).getId()).orElseThrow().getStatus())
                .isEqualTo(Seat.SeatStatus.TEMP_RESERVED);
    }
}
