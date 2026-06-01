package com.goosebeoms.tickets.domain.show.controller;

import com.goosebeoms.tickets.domain.show.dto.AdminScheduleCreateRequest;
import com.goosebeoms.tickets.domain.show.dto.AdminScheduleResponse;
import com.goosebeoms.tickets.domain.show.dto.AdminScheduleUpdateRequest;
import com.goosebeoms.tickets.domain.show.service.AdminScheduleService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin / Schedules", description = "관리자 회차 생성·수정·삭제 · 좌석 자동 시딩")
@RestController
@RequestMapping("/admin/schedules")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    @Operation(summary = "회차 생성",
            description = "zones[] 의 rowCount x columnCount 만큼 Seat 자동 생성 (status=AVAILABLE). totalCapacity는 합계로 자동 계산.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminScheduleResponse> create(@Valid @RequestBody AdminScheduleCreateRequest request) {
        return ApiResponse.ok(adminScheduleService.create(request));
    }

    @Operation(summary = "회차 수정", description = "scheduledAt 만 변경 가능. 좌석 구성 변경은 새 회차로 처리 권장.")
    @PatchMapping("/{scheduleId}")
    public ApiResponse<AdminScheduleResponse> update(
            @PathVariable Long scheduleId,
            @Valid @RequestBody AdminScheduleUpdateRequest request
    ) {
        return ApiResponse.ok(adminScheduleService.update(scheduleId, request));
    }

    @Operation(summary = "회차 삭제", description = "예매 내역이 있으면 409 SCHEDULE_HAS_BOOKINGS")
    @DeleteMapping("/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long scheduleId) {
        adminScheduleService.delete(scheduleId);
    }

    @Operation(summary = "회차 단건 조회 (관리자)")
    @GetMapping("/{scheduleId}")
    public ApiResponse<AdminScheduleResponse> get(@PathVariable Long scheduleId) {
        return ApiResponse.ok(adminScheduleService.get(scheduleId));
    }

    @Operation(summary = "공연별 회차 목록", description = "showId 필수")
    @GetMapping
    public ApiResponse<List<AdminScheduleResponse>> listByShow(@RequestParam Long showId) {
        return ApiResponse.ok(adminScheduleService.listByShow(showId));
    }
}
