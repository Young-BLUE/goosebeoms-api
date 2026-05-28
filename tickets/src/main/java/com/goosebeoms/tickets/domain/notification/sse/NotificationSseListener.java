package com.goosebeoms.tickets.domain.notification.sse;

import com.goosebeoms.tickets.domain.notification.event.NotificationCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationSseListener {

    private final NotificationSseRegistry registry;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        registry.send(event.userId(), "notification", event.payload());
    }
}
