package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;

import java.time.LocalDateTime;

public record ShowScheduleResponse(
        Long id,
        LocalDateTime scheduledAt,
        int totalCapacity,
        int availableCount,
        String status,
        String statusLabel
) {
    public static ShowScheduleResponse from(ShowSchedule schedule) {
        return new ShowScheduleResponse(
                schedule.getId(),
                schedule.getScheduledAt(),
                schedule.getTotalCapacity(),
                schedule.getAvailableCount(),
                schedule.getStatus().name(),
                schedule.getStatus().getLabel()
        );
    }
}
