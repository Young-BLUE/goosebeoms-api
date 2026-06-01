package com.goosebeoms.tickets.domain.coupon.controller;

import com.goosebeoms.tickets.domain.coupon.dto.AdminCouponCreateRequest;
import com.goosebeoms.tickets.domain.coupon.dto.AdminCouponUpdateRequest;
import com.goosebeoms.tickets.domain.coupon.dto.CouponResponse;
import com.goosebeoms.tickets.domain.coupon.service.AdminCouponService;
import com.goosebeoms.tickets.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin / Coupons", description = "관리자 쿠폰 생성·수정·즉시 만료")
@RestController
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    @Operation(summary = "쿠폰 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CouponResponse> create(@Valid @RequestBody AdminCouponCreateRequest request) {
        return ApiResponse.ok(adminCouponService.create(request));
    }

    @Operation(summary = "쿠폰 부분 수정", description = "name / maxCount / validUntil 만 변경 가능")
    @PatchMapping("/{couponId}")
    public ApiResponse<CouponResponse> update(
            @PathVariable Long couponId,
            @Valid @RequestBody AdminCouponUpdateRequest request
    ) {
        return ApiResponse.ok(adminCouponService.update(couponId, request));
    }

    @Operation(summary = "쿠폰 즉시 만료", description = "validUntil을 현재 시각으로 설정해 발급/사용 모두 차단")
    @PostMapping("/{couponId}/expire")
    public ApiResponse<CouponResponse> expire(@PathVariable Long couponId) {
        return ApiResponse.ok(adminCouponService.expire(couponId));
    }
}
