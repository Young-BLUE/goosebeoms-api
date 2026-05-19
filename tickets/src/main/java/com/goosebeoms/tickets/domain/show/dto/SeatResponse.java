package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.Seat;

public record SeatResponse(
        Long id,
        String rowLabel,
        int number,
        String status
) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getRowLabel(),
                seat.getNumber(),
                seat.getStatus().name()
        );
    }
}
