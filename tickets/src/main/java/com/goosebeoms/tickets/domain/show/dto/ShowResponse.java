package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.Show;

public record ShowResponse(
        Long id,
        String title,
        String venue,
        String category,
        String categoryLabel,
        String posterUrl,
        String status,
        String statusLabel,
        int minPrice,
        int maxPrice
) {
    public static ShowResponse from(Show show) {
        return new ShowResponse(
                show.getId(),
                show.getTitle(),
                show.getVenue(),
                show.getCategory().name(),
                show.getCategory().getLabel(),
                show.getPosterUrl(),
                show.getStatus().name(),
                show.getStatus().getLabel(),
                show.getMinPrice(),
                show.getMaxPrice()
        );
    }
}
