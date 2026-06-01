package com.goosebeoms.tickets.domain.booking.dto;

import jakarta.validation.constraints.Size;

public record AdminBookingCancelRequest(
        @Size(max = 200) String reason
) {}
