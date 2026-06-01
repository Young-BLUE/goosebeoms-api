package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.Show;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record AdminShowCreateRequest(
        @NotBlank String title,
        String description,
        @NotBlank String venue,
        @NotNull Show.Category category,
        @NotNull Show.Status status,
        String posterUrl,
        @PositiveOrZero int minPrice,
        @PositiveOrZero int maxPrice,
        @NotNull LocalDateTime bookingStartAt,
        @NotNull LocalDateTime bookingEndAt
) {}
