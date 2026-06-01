package com.goosebeoms.tickets.domain.show.controller;

import com.goosebeoms.tickets.domain.show.dto.AdminShowCreateRequest;
import com.goosebeoms.tickets.domain.show.dto.AdminShowStatusRequest;
import com.goosebeoms.tickets.domain.show.dto.AdminShowUpdateRequest;
import com.goosebeoms.tickets.domain.show.dto.ShowResponse;
import com.goosebeoms.tickets.domain.show.service.AdminShowService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin / Shows", description = "관리자 공연 생성·수정·상태 변경")
@RestController
@RequestMapping("/admin/shows")
@RequiredArgsConstructor
public class AdminShowController {

    private final AdminShowService adminShowService;

    @Operation(summary = "공연 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShowResponse> create(@Valid @RequestBody AdminShowCreateRequest request) {
        return ApiResponse.ok(adminShowService.create(request));
    }

    @Operation(summary = "공연 부분 수정", description = "전달된 필드만 갱신")
    @PatchMapping("/{showId}")
    public ApiResponse<ShowResponse> update(
            @PathVariable Long showId,
            @Valid @RequestBody AdminShowUpdateRequest request
    ) {
        return ApiResponse.ok(adminShowService.update(showId, request));
    }

    @Operation(summary = "공연 상태 변경", description = "UPCOMING / ON_SALE / SOLD_OUT / ENDED")
    @PatchMapping("/{showId}/status")
    public ApiResponse<ShowResponse> updateStatus(
            @PathVariable Long showId,
            @Valid @RequestBody AdminShowStatusRequest request
    ) {
        return ApiResponse.ok(adminShowService.updateStatus(showId, request.status()));
    }
}
