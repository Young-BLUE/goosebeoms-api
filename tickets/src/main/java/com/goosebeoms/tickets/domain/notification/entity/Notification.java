package com.goosebeoms.tickets.domain.notification.entity;

import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_created", columnList = "user_id, createdAt"),
        @Index(name = "idx_notification_user_read", columnList = "user_id, isRead")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    private LocalDateTime readAt;

    private String resourceType;
    private Long resourceId;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        BOOKING_CONFIRMED("예매 완료"),
        BOOKING_CANCELLED("예매 취소"),
        BOOKING_EXPIRED("선점 만료"),
        SYSTEM("시스템");

        private final String label;
    }

    @Builder
    private Notification(User user, Type type, String title, String message,
                         String resourceType, Long resourceId) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public void markRead() {
        if (this.isRead) return;
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
