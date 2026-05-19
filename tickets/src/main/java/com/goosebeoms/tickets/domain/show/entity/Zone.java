package com.goosebeoms.tickets.domain.show.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "zones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_schedule_id", nullable = false)
    private ShowSchedule showSchedule;

    @Column(nullable = false)
    private String name;

    private int price;

    private int rowCount;

    private int columnCount;

    @Builder
    private Zone(ShowSchedule showSchedule, String name, int price, int rowCount, int columnCount) {
        this.showSchedule = showSchedule;
        this.name = name;
        this.price = price;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }
}
