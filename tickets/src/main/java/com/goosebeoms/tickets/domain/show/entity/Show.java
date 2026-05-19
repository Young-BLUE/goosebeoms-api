package com.goosebeoms.tickets.domain.show.entity;

import com.goosebeoms.tickets.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public enum Category { MUSICAL, CONCERT, SPORTS, THEATER }
    public enum Status { UPCOMING, ON_SALE, SOLD_OUT, ENDED }

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
