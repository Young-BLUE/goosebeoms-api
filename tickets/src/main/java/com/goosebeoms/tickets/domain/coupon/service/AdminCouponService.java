package com.goosebeoms.tickets.domain.coupon.service;

import com.goosebeoms.tickets.domain.coupon.dto.AdminCouponCreateRequest;
import com.goosebeoms.tickets.domain.coupon.dto.AdminCouponUpdateRequest;
import com.goosebeoms.tickets.domain.coupon.dto.CouponResponse;
import com.goosebeoms.tickets.domain.coupon.entity.Coupon;
import com.goosebeoms.tickets.domain.coupon.repository.CouponRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCouponService {

    private final CouponRepository couponRepository;

    public CouponResponse create(AdminCouponCreateRequest request) {
        try {
            Coupon coupon = couponRepository.save(Coupon.builder()
                    .code(request.code())
                    .name(request.name())
                    .discountType(request.discountType())
                    .discountValue(request.discountValue())
                    .maxCount(request.maxCount())
                    .validFrom(request.validFrom())
                    .validUntil(request.validUntil())
                    .build());
            return CouponResponse.from(coupon);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.COUPON_CODE_DUPLICATED);
        }
    }

    public CouponResponse update(Long couponId, AdminCouponUpdateRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        try {
            coupon.updateInfo(request.name(), request.maxCount(), request.validUntil());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.COUPON_MAX_COUNT_BELOW_ISSUED);
        }
        return CouponResponse.from(coupon);
    }

    public CouponResponse expire(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        coupon.expireNow();
        return CouponResponse.from(coupon);
    }
}
