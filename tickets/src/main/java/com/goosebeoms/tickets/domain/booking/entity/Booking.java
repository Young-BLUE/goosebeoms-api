package com.goosebeoms.tickets.domain.booking.entity;

import com.goosebeoms.tickets.domain.coupon.entity.UserCoupon;
import com.goosebeoms.tickets.domain.show.entity.ShowSchedule;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends BaseTimeEntity {

    public static final int HOLD_TTL_MINUTES = 7;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_schedule_id", nullable = false)
    private ShowSchedule showSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    private UserCoupon userCoupon;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private int originalPrice;
    private int discountPrice;
    private int finalPrice;

    @Column(nullable = false)
    private LocalDateTime holdExpiresAt;

    private LocalDateTime paidAt;

    @Getter
    public enum BookingStatus {
        HOLD("결제 대기"),
        CONFIRMED("결제 완료"),
        CANCELLED("취소"),
        EXPIRED("만료");

        private final String label;
        BookingStatus(String label) { this.label = label; }
    }

    @Builder
    private Booking(User user, ShowSchedule showSchedule, UserCoupon userCoupon,
                    int originalPrice, int discountPrice) {
        this.user = user;
        this.showSchedule = showSchedule;
        this.userCoupon = userCoupon;
        this.status = BookingStatus.HOLD;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.finalPrice = originalPrice - discountPrice;
        this.holdExpiresAt = LocalDateTime.now().plusMinutes(HOLD_TTL_MINUTES);
    }

    public void confirm() {
        if (this.status != BookingStatus.HOLD) {
            throw new IllegalStateException("HOLD 상태의 예매만 확정할 수 있습니다.");
        }
        this.status = BookingStatus.CONFIRMED;
        this.paidAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예매입니다.");
        }
        if (this.status == BookingStatus.EXPIRED) {
            throw new IllegalStateException("만료된 예매는 취소할 수 없습니다.");
        }
        this.status = BookingStatus.CANCELLED;
    }

    public void expire() {
        if (this.status != BookingStatus.HOLD) {
            throw new IllegalStateException("HOLD 상태의 예매만 만료 처리할 수 있습니다.");
        }
        this.status = BookingStatus.EXPIRED;
    }

    public boolean isHoldExpired(LocalDateTime now) {
        return this.status == BookingStatus.HOLD && this.holdExpiresAt.isBefore(now);
    }
}
