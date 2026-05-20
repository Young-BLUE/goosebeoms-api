package com.goosebeoms.tickets.domain.payment.entity;

import com.goosebeoms.tickets.domain.booking.entity.Booking;
import com.goosebeoms.tickets.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payments_booking", columnNames = "booking_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    private String providerTxnId;

    private LocalDateTime paidAt;

    public enum PaymentStatus { PENDING, SUCCESS, FAILED }
    public enum PaymentMethod { CARD, BANK_TRANSFER, MOCK }

    @Builder
    private Payment(Booking booking, int amount, PaymentMethod method) {
        this.booking = booking;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
    }

    public void markSuccess(String providerTxnId) {
        this.status = PaymentStatus.SUCCESS;
        this.providerTxnId = providerTxnId;
        this.paidAt = LocalDateTime.now();
    }

    public void markFailed(String providerTxnId) {
        this.status = PaymentStatus.FAILED;
        this.providerTxnId = providerTxnId;
    }
}
