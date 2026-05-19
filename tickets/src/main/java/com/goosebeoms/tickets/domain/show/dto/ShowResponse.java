package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.Show;

public record ShowResponse(
        Long id,
        String title,
        String venue,
        String category,
        String posterUrl,
        String status,
        int minPrice,
        int maxPrice
) {
    public static ShowResponse from(Show show) {
        return new ShowResponse(
                show.getId(),
                show.getTitle(),
                show.getVenue(),
                show.getCategory().name(),
                show.getPosterUrl(),
                show.getStatus().name(),
                show.getMinPrice(),
                show.getMaxPrice()
        );
    }
}
