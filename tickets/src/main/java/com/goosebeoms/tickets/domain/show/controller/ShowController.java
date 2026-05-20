package com.goosebeoms.tickets.domain.show.controller;

import com.goosebeoms.tickets.domain.show.dto.ShowDetailResponse;
import com.goosebeoms.tickets.domain.show.dto.ShowResponse;
import com.goosebeoms.tickets.domain.show.dto.ShowScheduleResponse;
import com.goosebeoms.tickets.domain.show.entity.Show;
import com.goosebeoms.tickets.domain.show.service.ShowService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Shows", description = "공연 목록 · 상세 · 회차")
@SecurityRequirements
@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping
    public ApiResponse<Page<ShowResponse>> getShows(
            @RequestParam(required = false) Show.Category category,
            @RequestParam(required = false) Show.Status status,
            @PageableDefault(size = 12, sort = "createdAt") Pageable pageable
    ) {
        return ApiResponse.ok(showService.getShows(category, status, pageable));
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
