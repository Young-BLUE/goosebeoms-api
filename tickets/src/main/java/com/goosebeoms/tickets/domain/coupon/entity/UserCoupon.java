package com.goosebeoms.tickets.domain.coupon.entity;

import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.global.enums.LabeledEnum;
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

    @Getter
    public enum Status implements LabeledEnum {
        AVAILABLE("사용 가능"),
        USED("사용 완료"),
        EXPIRED("만료");

        private final String label;
        Status(String label) { this.label = label; }
    }

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

    public void restore() {
        if (this.status != Status.USED) {
            throw new IllegalStateException("USED 상태의 쿠폰만 복원할 수 있습니다.");
        }
        this.status = Status.AVAILABLE;
        this.usedAt = null;
    }

    public boolean isUsable(LocalDateTime now) {
        if (this.status != Status.AVAILABLE) return false;
        LocalDateTime validUntil = this.coupon.getValidUntil();
        LocalDateTime validFrom = this.coupon.getValidFrom();
        return !now.isBefore(validFrom) && now.isBefore(validUntil);
    }
}
