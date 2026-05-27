package com.goosebeoms.tickets.domain.queue.dto;

public record QueueStatusResponse(
        State state,
        Long position,
        Long ahead,
        Long behind,
        Long totalWaiting,
        Long etaSeconds,
        String token,
        Long expiresAt
) {
    public enum State { WAITING, ACTIVE, NONE }

    public static QueueStatusResponse waiting(long position, long ahead, long behind, long totalWaiting, long etaSeconds) {
        return new QueueStatusResponse(State.WAITING, position, ahead, behind, totalWaiting, etaSeconds, null, null);
    }

    public static QueueStatusResponse active(String token, long expiresAt) {
        return new QueueStatusResponse(State.ACTIVE, null, null, null, null, null, token, expiresAt);
    }

    public static QueueStatusResponse none() {
        return new QueueStatusResponse(State.NONE, null, null, null, null, null, null, null);
    }
}
