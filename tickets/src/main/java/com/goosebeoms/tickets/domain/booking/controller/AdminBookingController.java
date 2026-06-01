package com.goosebeoms.tickets.domain.booking.controller;

import com.goosebeoms.tickets.domain.booking.dto.AdminBookingCancelRequest;
import com.goosebeoms.tickets.domain.booking.dto.AdminBookingDetailResponse;
import com.goosebeoms.tickets.domain.booking.dto.AdminBookingResponse;
import com.goosebeoms.tickets.domain.booking.dto.BookingCancelResponse;
import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.domain.booking.service.AdminBookingService;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Admin / Bookings", description = "관리자 예매 조회·강제 취소")
@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final AdminBookingService adminBookingService;
    private final BookingService bookingService;

    @Operation(summary = "예매 목록 (필터 + 페이지)",
            description = "userId, scheduleId, status, 생성일 범위로 필터. 정렬은 Pageable sort.")
    @GetMapping
    public ApiResponse<Page<AdminBookingResponse>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Booking.BookingStatus status,
            @Parameter(description = "생성일 하한 (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "생성일 상한")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok(adminBookingService.search(userId, scheduleId, status, from, to, pageable));
    }

    @Operation(summary = "예매 단건 상세", description = "좌석 목록 + 결제/환불 정보 포함")
    @GetMapping("/{bookingId}")
    public ApiResponse<AdminBookingDetailResponse> get(@PathVariable Long bookingId) {
        return ApiResponse.ok(adminBookingService.get(bookingId));
    }

    @Operation(summary = "강제 취소", description = "관리자 사유와 함께 강제 취소. 사용자에게 알림 + 환불 + 쿠폰 복원 동일 흐름.")
    @PostMapping("/{bookingId}/force-cancel")
    public ApiResponse<BookingCancelResponse> forceCancel(
            @PathVariable Long bookingId,
            @Valid @RequestBody AdminBookingCancelRequest request
    ) {
        return ApiResponse.ok(bookingService.forceCancel(bookingId, request.reason()));
    }
}
