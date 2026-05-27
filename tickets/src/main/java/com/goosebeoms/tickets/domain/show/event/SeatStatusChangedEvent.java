package com.goosebeoms.tickets.domain.show.event;

import com.goosebeoms.tickets.domain.show.entity.Seat;

import java.util.List;

public record SeatStatusChangedEvent(Long scheduleId, List<SeatChange> changes) {

    public record SeatChange(Long seatId, String status, String statusLabel) {
        public static SeatChange from(Seat seat) {
            return new SeatChange(
                    seat.getId(),
                    seat.getStatus().name(),
                    seat.getStatus().getLabel()
            );
        }
    }
}
