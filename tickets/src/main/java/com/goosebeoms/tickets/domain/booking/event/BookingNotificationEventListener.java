package com.goosebeoms.tickets.domain.booking.event;

import com.goosebeoms.tickets.domain.notification.service.NotificationService;
import com.goosebeoms.tickets.domain.user.entity.User;
import com.goosebeoms.tickets.domain.user.repository.UserRepository;
import com.goosebeoms.tickets.global.exception.BusinessException;
import com.goosebeoms.tickets.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BookingNotificationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingNotification(BookingNotificationEvent event) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        notificationService.create(
                user,
                event.type(),
                event.title(),
                event.message(),
                "BOOKING",
                event.bookingId()
        );
    }
}
