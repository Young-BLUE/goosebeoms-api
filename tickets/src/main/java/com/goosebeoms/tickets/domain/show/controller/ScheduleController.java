package com.goosebeoms.tickets.domain.show.controller;

import com.goosebeoms.tickets.domain.show.dto.SeatResponse;
import com.goosebeoms.tickets.domain.show.dto.ZoneResponse;
import com.goosebeoms.tickets.domain.show.service.ShowService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ShowService showService;

    @GetMapping("/{scheduleId}/zones")
    public ApiResponse<List<ZoneResponse>> getZones(@PathVariable Long scheduleId) {
        return ApiResponse.ok(showService.getZones(scheduleId));
    }

    @GetMapping("/{scheduleId}/seats")
    public ApiResponse<List<SeatResponse>> getSeats(
            @PathVariable Long scheduleId,
            @RequestParam(required = false) Long zoneId
    ) {
        return ApiResponse.ok(showService.getSeats(scheduleId, zoneId));
    }
}
