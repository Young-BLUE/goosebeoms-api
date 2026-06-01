package com.goosebeoms.tickets.domain.show.dto;

import java.time.LocalDateTime;

public record AdminScheduleUpdateRequest(
        LocalDateTime scheduledAt
) {}
