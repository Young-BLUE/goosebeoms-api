package com.goosebeoms.tickets.domain.coupon.entity;

import com.goosebeoms.tickets.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "coupon_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;

    public enum Status { AVAILABLE, USED, EXPIRED }

    @Builder
    private UserCoupon(User user, Coupon coupon) {
        this.user = user;
        this.coupon = coupon;
        this.status = Status.AVAILABLE;
        this.issuedAt = LocalDateTime.now();
    }

    public void use() {
        if (this.status != Status.AVAILABLE) {
            throw new IllegalStateException("사용 불가능한 쿠폰입니다.");
        }
        this.status = Status.USED;
        this.usedAt = LocalDateTime.now();
    }
}
