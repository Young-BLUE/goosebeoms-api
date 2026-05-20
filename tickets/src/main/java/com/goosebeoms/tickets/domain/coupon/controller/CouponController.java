package com.goosebeoms.tickets.domain.coupon.controller;

import com.goosebeoms.tickets.domain.coupon.dto.CouponResponse;
import com.goosebeoms.tickets.domain.coupon.dto.UserCouponResponse;
import com.goosebeoms.tickets.domain.coupon.service.CouponService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Coupons", description = "쿠폰 발급 (선착순 CAS) · 보유 쿠폰")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "발급 가능한 쿠폰 목록", description = "유효기간 내 쿠폰만 노출")
    @SecurityRequirements
    @GetMapping
    public ApiResponse<List<CouponResponse>> getAvailableCoupons() {
        return ApiResponse.ok(couponService.getAvailableCoupons());
    }

    @Operation(summary = "쿠폰 선착순 발급", description = "DB 조건부 UPDATE(CAS)로 maxCount 초과 차단. 1인 1매")
    @PostMapping("/{couponId}/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserCouponResponse> issue(
            @PathVariable Long couponId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(couponService.issue(couponId, userDetails.getUsername()));
    }

    @Operation(summary = "내 보유 쿠폰")
    @GetMapping("/me")
    public ApiResponse<List<UserCouponResponse>> getMyCoupons(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(couponService.getMyCoupons(userDetails.getUsername()));
    }
}
