package com.goosebeoms.tickets.domain.coupon.service;

import com.goosebeoms.tickets.domain.coupon.dto.CouponResponse;
import com.goosebeoms.tickets.domain.coupon.dto.UserCouponResponse;
import com.goosebeoms.tickets.domain.coupon.entity.Coupon;
import com.goosebeoms.tickets.domain.coupon.entity.UserCoupon;
import com.goosebeoms.tickets.domain.coupon.repository.CouponRepository;
import com.goosebeoms.tickets.domain.coupon.repository.UserCouponRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    public List<CouponResponse> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByValidFromBeforeAndValidUntilAfter(now, now).stream()
                .map(CouponResponse::from)
                .toList();
    }

    public List<UserCouponResponse> getMyCoupons(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return userCouponRepository.findAvailableByUserId(user.getId()).stream()
                .map(UserCouponResponse::from)
                .toList();
    }

    @Transactional
    public UserCouponResponse issue(Long couponId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || !now.isBefore(coupon.getValidUntil())) {
            throw new BusinessException(ErrorCode.COUPON_NOT_AVAILABLE);
        }

        int updated = couponRepository.tryIncreaseIssuedCount(couponId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
        }

        try {
            UserCoupon userCoupon = userCouponRepository.saveAndFlush(UserCoupon.builder()
                    .user(user)
                    .coupon(coupon)
                    .build());
            return UserCouponResponse.from(userCoupon);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }
    }
}
