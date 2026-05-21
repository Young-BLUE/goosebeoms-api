package com.goosebeoms.tickets.domain.booking.controller;

import com.goosebeoms.tickets.domain.booking.dto.BookingCancelResponse;
import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.dto.BookingResponse;
import com.goosebeoms.tickets.domain.booking.dto.BookingSummaryResponse;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import com.goosebeoms.tickets.domain.payment.dto.PaymentConfirmRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareResponse;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bookings & Payment", description = "좌석 hold → 결제 prepare/confirm → 취소 흐름")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "좌석 hold", description = "대기열 통과 토큰(X-Queue-Token) 필수")
    @SecurityRequirements({@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "queueToken")})
    @PostMapping("/hold")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingResponse> hold(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader(value = "X-Queue-Token", required = false) String queueToken,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.hold(userDetails.getUsername(), request, queueToken));
    }

    @Operation(summary = "결제 prepare", description = "Payment(PENDING) 생성 + orderId 발급. 클라이언트는 응답값으로 토스 결제창 호출")
    @PostMapping("/{bookingId}/payment/prepare")
    public ApiResponse<PaymentPrepareResponse> preparePayment(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentPrepareRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.preparePayment(bookingId, userDetails.getUsername(), request));
    }

    @Operation(summary = "결제 confirm", description = "토스 success redirect에서 받은 paymentKey/orderId/amount로 승인 마무리")
    @PostMapping("/{bookingId}/payment/confirm")
    public ApiResponse<BookingResponse> confirmPayment(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.confirmPayment(bookingId, userDetails.getUsername(), request));
    }

    @Operation(summary = "내 예매 목록")
    @GetMapping("/me")
    public ApiResponse<List<BookingSummaryResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.getMyBookings(userDetails.getUsername()));
    }

    @Operation(summary = "예매 상세")
    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> getBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.getBooking(bookingId, userDetails.getUsername()));
    }

    @Operation(summary = "예매 취소",
            description = "HOLD/CONFIRMED 상태에서만 가능. CONFIRMED 취소 시 쿠폰 복원 — 유효기간 만료된 경우 복원되지 않고 응답에 안내")
    @DeleteMapping("/{bookingId}")
    public ApiResponse<BookingCancelResponse> cancel(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.cancel(bookingId, userDetails.getUsername()));
    }
}
