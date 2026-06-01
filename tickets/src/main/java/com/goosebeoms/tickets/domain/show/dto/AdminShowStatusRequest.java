package com.goosebeoms.tickets.domain.show.dto;

import com.goosebeoms.tickets.domain.show.entity.Show;
import jakarta.validation.constraints.NotNull;

public record AdminShowStatusRequest(@NotNull Show.Status status) {}
