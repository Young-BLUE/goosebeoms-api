package com.goosebeoms.tickets.domain.booking.controller;

import com.goosebeoms.tickets.domain.booking.dto.BookingRequest;
import com.goosebeoms.tickets.domain.booking.dto.BookingResponse;
import com.goosebeoms.tickets.domain.booking.dto.BookingSummaryResponse;
import com.goosebeoms.tickets.domain.booking.service.BookingService;
import com.goosebeoms.tickets.domain.payment.dto.PaymentConfirmRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareRequest;
import com.goosebeoms.tickets.domain.payment.dto.PaymentPrepareResponse;
import com.goosebeoms.tickets.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/hold")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingResponse> hold(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader(value = "X-Queue-Token", required = false) String queueToken,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.hold(userDetails.getUsername(), request, queueToken));
    }

    @PostMapping("/{bookingId}/payment/prepare")
    public ApiResponse<PaymentPrepareResponse> preparePayment(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentPrepareRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.preparePayment(bookingId, userDetails.getUsername(), request));
    }

    @PostMapping("/{bookingId}/payment/confirm")
    public ApiResponse<BookingResponse> confirmPayment(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.confirmPayment(bookingId, userDetails.getUsername(), request));
    }

    @GetMapping("/me")
    public ApiResponse<List<BookingSummaryResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.getMyBookings(userDetails.getUsername()));
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> getBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(bookingService.getBooking(bookingId, userDetails.getUsername()));
    }

    @DeleteMapping("/{bookingId}")
    public ApiResponse<Void> cancel(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        bookingService.cancel(bookingId, userDetails.getUsername());
        return ApiResponse.ok();
    }
}
