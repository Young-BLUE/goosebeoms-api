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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

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
        RLock lock = redissonClient.getLock("coupon:issue:" + couponId);
        try {
            boolean acquired = lock.tryLock(5, 3, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            if (userCouponRepository.existsByUserIdAndCouponId(user.getId(), couponId)) {
                throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
            }

            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

            if (!coupon.isIssuable()) {
                throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
            }

            coupon.increaseIssuedCount();

            UserCoupon userCoupon = userCouponRepository.save(UserCoupon.builder()
                    .user(user)
                    .coupon(coupon)
                    .build());

            return UserCouponResponse.from(userCoupon);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.COUPON_EXHAUSTED);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
