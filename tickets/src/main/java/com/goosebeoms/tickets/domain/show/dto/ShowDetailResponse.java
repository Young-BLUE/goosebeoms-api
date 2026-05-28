package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.Show;

import java.time.LocalDateTime;
import java.util.List;

public record ShowDetailResponse(
        Long id,
        String title,
        String description,
        String venue,
        String category,
        String categoryLabel,
        String posterUrl,
        String status,
        String statusLabel,
        int minPrice,
        int maxPrice,
        LocalDateTime bookingStartAt,
        LocalDateTime bookingEndAt,
        List<ShowScheduleResponse> schedules
) {
    public static ShowDetailResponse from(Show show, List<ShowScheduleResponse> schedules) {
        return new ShowDetailResponse(
                show.getId(),
                show.getTitle(),
                show.getDescription(),
                show.getVenue(),
                show.getCategory().name(),
                show.getCategory().getLabel(),
                show.getPosterUrl(),
                show.getStatus().name(),
                show.getStatus().getLabel(),
                show.getMinPrice(),
                show.getMaxPrice(),
                show.getBookingStartAt(),
                show.getBookingEndAt(),
                schedules
        );
    }
}
