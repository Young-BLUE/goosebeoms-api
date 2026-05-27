package com.goosebeoms.tickets.domain.show.sse;

import com.goosebeoms.tickets.domain.show.event.SeatStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SeatSseListener {

    private final SeatSseRegistry registry;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSeatStatusChanged(SeatStatusChangedEvent event) {
        registry.broadcast(event.scheduleId(), "seat-status", event);
    }
}
