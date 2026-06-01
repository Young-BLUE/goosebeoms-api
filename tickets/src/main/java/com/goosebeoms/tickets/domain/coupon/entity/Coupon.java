package com.goosebeoms.tickets.domain.coupon.entity;

import com.goosebeoms.tickets.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    private int discountValue;

    private int maxCount;

    private int issuedCount;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    @Getter
    public enum DiscountType {
        FIXED("정액 할인"),
        PERCENTAGE("정률 할인");

        private final String label;
        DiscountType(String label) { this.label = label; }
    }

    @Builder
    private Coupon(String code, String name, DiscountType discountType, int discountValue,
                   int maxCount, LocalDateTime validFrom, LocalDateTime validUntil) {
        this.code = code;
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxCount = maxCount;
        this.issuedCount = 0;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public boolean isIssuable() {
        return this.issuedCount < this.maxCount;
    }

    public void increaseIssuedCount() {
        if (!isIssuable()) {
            throw new IllegalStateException("쿠폰 발급 한도를 초과했습니다.");
        }
        this.issuedCount++;
    }

    public int calculateDiscount(int originalPrice) {
        return switch (this.discountType) {
            case FIXED -> Math.min(this.discountValue, originalPrice);
            case PERCENTAGE -> originalPrice * this.discountValue / 100;
        };
    }

    public void updateInfo(String name, Integer maxCount, LocalDateTime validUntil) {
        if (name != null) this.name = name;
        if (maxCount != null) {
            if (maxCount < this.issuedCount) {
                throw new IllegalArgumentException("maxCount는 이미 발급된 수량보다 작을 수 없습니다.");
            }
            this.maxCount = maxCount;
        }
        if (validUntil != null) this.validUntil = validUntil;
    }

    public void expireNow() {
        this.validUntil = LocalDateTime.now();
    }
}
