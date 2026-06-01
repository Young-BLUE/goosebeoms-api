package com.goosebeoms.tickets.domain.admin.controller;

import com.goosebeoms.tickets.domain.admin.dto.AdminStatsResponse;
import com.goosebeoms.tickets.domain.admin.service.AdminStatsService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin / Stats", description = "관리자 대시보드 통계")
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @Operation(summary = "대시보드 통계",
            description = "총 예매수 / 오늘 예매수 / 총 매출(CONFIRMED 기준) / 오늘 매출 / 판매중 공연 / 매진 공연 / 총 사용자 / 활성 쿠폰")
    @GetMapping
    public ApiResponse<AdminStatsResponse> get() {
        return ApiResponse.ok(adminStatsService.get());
    }
}
