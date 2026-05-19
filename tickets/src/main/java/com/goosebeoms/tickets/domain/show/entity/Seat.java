package com.goosebeoms.tickets.domain.show.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false)
    private String rowLabel;

    @Column(nullable = false)
    private int number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    public enum SeatStatus { AVAILABLE, TEMP_RESERVED, SOLD }

    @Builder
    private Seat(Zone zone, String rowLabel, int number) {
        this.zone = zone;
        this.rowLabel = rowLabel;
        this.number = number;
        this.status = SeatStatus.AVAILABLE;
    }

    public void tempReserve() {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("이미 선점된 좌석입니다.");
        }
        this.status = SeatStatus.TEMP_RESERVED;
    }

    public void confirm() {
        this.status = SeatStatus.SOLD;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
    }
}
