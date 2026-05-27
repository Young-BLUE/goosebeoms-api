package com.goosebeoms.tickets.domain.show.controller;

import com.goosebeoms.tickets.domain.show.dto.SeatResponse;
import com.goosebeoms.tickets.domain.show.dto.ZoneResponse;
import com.goosebeoms.tickets.domain.show.service.ShowService;
import com.goosebeoms.tickets.domain.show.sse.SeatSseRegistry;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "Schedules", description = "회차별 존 · 좌석맵 · 좌석 상태 SSE")
@SecurityRequirements
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ShowService showService;
    private final SeatSseRegistry seatSseRegistry;

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

    @Operation(summary = "좌석 상태 SSE 구독",
            description = "다른 사용자의 hold / 결제 / 취소 / 만료 시 변경된 좌석 정보를 'seat-status' 이벤트로 push. 초기 스냅샷은 /seats로 별도 페치.")
    @GetMapping(value = "/{scheduleId}/seats/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeSeats(@PathVariable Long scheduleId) {
        SseEmitter emitter = new SseEmitter(0L);
        seatSseRegistry.register(scheduleId, emitter);
        return emitter;
    }
}
