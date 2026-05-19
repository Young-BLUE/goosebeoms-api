package com.goosebeoms.tickets.domain.coupon.repository;

import com.goosebeoms.tickets.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    List<Coupon> findByValidFromBeforeAndValidUntilAfter(LocalDateTime from, LocalDateTime until);
}
