package com.goosebeoms.tickets.domain.show.controller;

import com.goosebeoms.tickets.domain.show.dto.ShowDetailResponse;
import com.goosebeoms.tickets.domain.show.dto.ShowResponse;
import com.goosebeoms.tickets.domain.show.dto.ShowScheduleResponse;
import com.goosebeoms.tickets.domain.show.entity.Show;
import com.goosebeoms.tickets.domain.show.service.ShowService;
import com.goosebeoms.tickets.domain.show.service.ShowService.ShowSearchCondition;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Shows", description = "공연 목록 · 검색 · 상세 · 회차")
@SecurityRequirements
@RestController
@RequestMapping("/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @Operation(summary = "공연 목록 검색",
            description = "정렬은 Pageable sort 파라미터로 지정 (예: ?sort=minPrice,asc / ?sort=createdAt,desc). 허용 필드: createdAt, title, minPrice, maxPrice")
    @GetMapping
    public ApiResponse<Page<ShowResponse>> getShows(
            @Parameter(description = "제목 키워드 (부분 일치, 대소문자 무시)")
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Show.Category category,
            @RequestParam(required = false) Show.Status status,
            @Parameter(description = "최소 가격 (이 가격 이상의 좌석이 있는 공연만)")
            @RequestParam(required = false) Integer minPrice,
            @Parameter(description = "최대 가격 (이 가격 이하의 좌석이 있는 공연만)")
            @RequestParam(required = false) Integer maxPrice,
            @Parameter(description = "회차 시작 시각 하한 (ISO-8601, 예: 2026-06-01T00:00:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "회차 시작 시각 상한")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ShowSearchCondition cond = new ShowSearchCondition(q, category, status, minPrice, maxPrice, dateFrom, dateTo);
        return ApiResponse.ok(showService.getShows(cond, pageable));
    }

    @GetMapping("/{showId}")
    public ApiResponse<ShowDetailResponse> getShow(@PathVariable Long showId) {
        return ApiResponse.ok(showService.getShow(showId));
    }

    @GetMapping("/{showId}/schedules")
    public ApiResponse<List<ShowScheduleResponse>> getSchedules(@PathVariable Long showId) {
        return ApiResponse.ok(showService.getSchedules(showId));
    }
}
