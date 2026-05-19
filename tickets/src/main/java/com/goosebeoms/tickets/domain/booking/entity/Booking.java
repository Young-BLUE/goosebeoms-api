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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends BaseTimeEntity {

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

    public enum BookingStatus { PENDING, CONFIRMED, CANCELLED }

    @Builder
    private Booking(User user, ShowSchedule showSchedule, UserCoupon userCoupon,
                    int originalPrice, int discountPrice) {
        this.user = user;
        this.showSchedule = showSchedule;
        this.userCoupon = userCoupon;
        this.status = BookingStatus.PENDING;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.finalPrice = originalPrice - discountPrice;
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예매입니다.");
        }
        this.status = BookingStatus.CANCELLED;
    }
}
