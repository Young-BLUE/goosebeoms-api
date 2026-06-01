package com.goosebeoms.tickets.domain.show.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record AdminScheduleCreateRequest(
        @NotNull Long showId,
        @NotNull LocalDateTime scheduledAt,
        @NotEmpty @Size(max = 10) @Valid List<ZoneSpec> zones
) {
    public record ZoneSpec(
            @NotBlank @Size(max = 20) String name,
            @PositiveOrZero int price,
            @Min(1) @Max(26) int rowCount,
            @Min(1) @Max(50) int columnCount
    ) {
        public int seatCount() {
            return rowCount * columnCount;
        }
    }
}
