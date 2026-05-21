package com.goosebeoms.tickets.domain.show.entity;

import com.goosebeoms.tickets.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Show extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private String posterUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private int minPrice;
    private int maxPrice;

    @Getter
    @RequiredArgsConstructor
    public enum Category {
        MUSICAL("뮤지컬"),
        CONCERT("콘서트"),
        SPORTS("스포츠"),
        THEATER("연극");

        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        UPCOMING("예매 예정"),
        ON_SALE("예매 중"),
        SOLD_OUT("매진"),
        ENDED("종료");

        private final String label;
    }

    @Builder
    private Show(String title, String description, String venue, Category category,
                 String posterUrl, Status status, int minPrice, int maxPrice) {
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.category = category;
        this.posterUrl = posterUrl;
        this.status = status;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }
}
