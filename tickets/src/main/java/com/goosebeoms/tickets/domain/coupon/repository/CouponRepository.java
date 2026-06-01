package com.goosebeoms.tickets.domain.coupon.repository;

import com.goosebeoms.tickets.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    List<Coupon> findByValidFromBeforeAndValidUntilAfter(LocalDateTime from, LocalDateTime until);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Coupon c SET c.issuedCount = c.issuedCount + 1 " +
            "WHERE c.id = :id AND c.issuedCount < c.maxCount")
    int tryIncreaseIssuedCount(@Param("id") Long id);

    long countByValidUntilAfter(LocalDateTime now);
}
