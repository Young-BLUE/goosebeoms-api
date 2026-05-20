package com.goosebeoms.tickets.domain.payment.client;

import lombok.Getter;

@Getter
public class TossApiException extends RuntimeException {

    private final String code;

    public TossApiException(String code, String message) {
        super(message);
        this.code = code;
    }
}
