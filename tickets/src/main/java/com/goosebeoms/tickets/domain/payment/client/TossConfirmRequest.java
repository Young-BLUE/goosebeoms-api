package com.goosebeoms.tickets.domain.payment.client;

public record TossConfirmRequest(String paymentKey, String orderId, int amount) {}
