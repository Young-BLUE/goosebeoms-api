package com.goosebeoms.tickets.domain.coupon.repository;

import com.goosebeoms.tickets.domain.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findByUserId(Long userId);
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.user.id = :userId AND uc.status = 'AVAILABLE'")
    List<UserCoupon> findAvailableByUserId(@Param("userId") Long userId);

    Optional<UserCoupon> findByIdAndUserId(Long id, Long userId);
}
