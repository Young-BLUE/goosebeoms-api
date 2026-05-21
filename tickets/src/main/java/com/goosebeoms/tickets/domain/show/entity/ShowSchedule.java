package com.goosebeoms.tickets.domain.show.entity;

import com.goosebeoms.tickets.global.entity.BaseTimeEntity;
import com.goosebeoms.tickets.global.enums.LabeledEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private int totalCapacity;

    private int availableCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // 낙관적 락 - 좌석 동시 예매 시 availableCount 충돌 방지
    @Version
    private Long version;

    @Getter
    public enum Status implements LabeledEnum {
        AVAILABLE("예매 가능"),
        SOLD_OUT("매진"),
        CANCELLED("취소");

        private final String label;
        Status(String label) { this.label = label; }
    }

    @Builder
    private ShowSchedule(Show show, LocalDateTime scheduledAt, int totalCapacity) {
        this.show = show;
        this.scheduledAt = scheduledAt;
        this.totalCapacity = totalCapacity;
        this.availableCount = totalCapacity;
        this.status = Status.AVAILABLE;
    }

    public void decreaseAvailableCount(int count) {
        if (this.availableCount < count) {
            throw new IllegalStateException("잔여 좌석이 부족합니다.");
        }
        this.availableCount -= count;
        if (this.availableCount == 0) {
            this.status = Status.SOLD_OUT;
        }
    }

    public void increaseAvailableCount(int count) {
        this.availableCount += count;
        if (this.status == Status.SOLD_OUT) {
            this.status = Status.AVAILABLE;
        }
    }
}
