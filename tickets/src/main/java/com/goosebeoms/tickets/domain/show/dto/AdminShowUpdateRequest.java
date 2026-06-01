package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.Show;

import java.time.LocalDateTime;

public record AdminShowUpdateRequest(
        String title,
        String description,
        String venue,
        Show.Category category,
        String posterUrl,
        Integer minPrice,
        Integer maxPrice,
        LocalDateTime bookingStartAt,
        LocalDateTime bookingEndAt
) {}
