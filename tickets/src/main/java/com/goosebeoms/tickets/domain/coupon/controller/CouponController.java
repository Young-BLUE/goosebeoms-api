package com.goosebeoms.tickets.domain.coupon.controller;

import com.goosebeoms.tickets.domain.coupon.dto.CouponResponse;
import com.goosebeoms.tickets.domain.coupon.dto.UserCouponResponse;
import com.goosebeoms.tickets.domain.coupon.service.CouponService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ApiResponse<List<CouponResponse>> getAvailableCoupons() {
        return ApiResponse.ok(couponService.getAvailableCoupons());
    }

    @PostMapping("/{couponId}/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserCouponResponse> issue(
            @PathVariable Long couponId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(couponService.issue(couponId, userDetails.getUsername()));
    }

    @GetMapping("/me")
    public ApiResponse<List<UserCouponResponse>> getMyCoupons(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(couponService.getMyCoupons(userDetails.getUsername()));
    }
}
