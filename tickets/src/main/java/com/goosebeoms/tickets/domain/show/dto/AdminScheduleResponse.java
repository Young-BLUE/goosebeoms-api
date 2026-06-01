package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.show.entity.Zone;

import java.time.LocalDateTime;
import java.util.List;

public record AdminScheduleResponse(
        Long id,
        Long showId,
        String showTitle,
        LocalDateTime scheduledAt,
        int totalCapacity,
        int availableCount,
        String status,
        String statusLabel,
        List<ZoneResponse> zones
) {
    public static AdminScheduleResponse from(ShowSchedule schedule, List<Zone> zones, List<Long> availablePerZone) {
        List<ZoneResponse> zoneResponses = java.util.stream.IntStream.range(0, zones.size())
                .mapToObj(i -> ZoneResponse.from(zones.get(i), availablePerZone.get(i)))
                .toList();
        return new AdminScheduleResponse(
                schedule.getId(),
                schedule.getShow().getId(),
                schedule.getShow().getTitle(),
                schedule.getScheduledAt(),
                schedule.getTotalCapacity(),
                schedule.getAvailableCount(),
                schedule.getStatus().name(),
                schedule.getStatus().getLabel(),
                zoneResponses
        );
    }
}
