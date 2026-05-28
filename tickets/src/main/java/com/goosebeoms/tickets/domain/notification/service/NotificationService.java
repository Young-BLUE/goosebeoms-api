package com.goosebeoms.tickets.domain.notification.service;

import com.goosebeoms.tickets.domain.notification.dto.NotificationResponse;
import com.goosebeoms.tickets.domain.notification.entity.Notification;
import com.goosebeoms.tickets.domain.notification.event.NotificationCreatedEvent;
import com.goosebeoms.tickets.domain.notification.repository.NotificationRepository;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void create(User user, Notification.Type type, String title, String message,
                       String resourceType, Long resourceId) {
        Notification saved = notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .build());

        eventPublisher.publishEvent(
                new NotificationCreatedEvent(user.getId(), NotificationResponse.from(saved)));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(String email, Pageable pageable) {
        Long userId = userIdOrThrow(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        Long userId = userIdOrThrow(email);
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markRead(Long notificationId, String email) {
        Long userId = userIdOrThrow(email);
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!n.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        n.markRead();
    }

    public void markAllRead(String email) {
        Long userId = userIdOrThrow(email);
        notificationRepository.markAllRead(userId, LocalDateTime.now());
    }

    private Long userIdOrThrow(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
