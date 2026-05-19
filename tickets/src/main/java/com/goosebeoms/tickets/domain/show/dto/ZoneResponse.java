package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.Zone;

public record ZoneResponse(
        Long id,
        String name,
        int price,
        int rowCount,
        int columnCount,
        long availableSeats
) {
    public static ZoneResponse from(Zone zone, long availableSeats) {
        return new ZoneResponse(
                zone.getId(),
                zone.getName(),
                zone.getPrice(),
                zone.getRowCount(),
                zone.getColumnCount(),
                availableSeats
        );
    }
}
