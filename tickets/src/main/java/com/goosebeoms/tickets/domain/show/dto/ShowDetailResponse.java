package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.Show;

import java.util.List;

public record ShowDetailResponse(
        Long id,
        String title,
        String description,
        String venue,
        String category,
        String posterUrl,
        String status,
        int minPrice,
        int maxPrice,
        List<ShowScheduleResponse> schedules
) {
    public static ShowDetailResponse from(Show show, List<ShowScheduleResponse> schedules) {
        return new ShowDetailResponse(
                show.getId(),
                show.getTitle(),
                show.getDescription(),
                show.getVenue(),
                show.getCategory().name(),
                show.getPosterUrl(),
                show.getStatus().name(),
                show.getMinPrice(),
                show.getMaxPrice(),
                schedules
        );
    }
}
