package com.goosebeoms.tickets.domain.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.payment.gateway", havingValue = "toss")
public class TossPaymentClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RestClient tossRestClient;

    public TossConfirmResponse confirm(String paymentKey, String orderId, int amount) {
        TossConfirmRequest body = new TossConfirmRequest(paymentKey, orderId, amount);
        return tossRestClient.post()
                .uri("/v1/payments/confirm")
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    String raw = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                    TossErrorResponse err = parseError(raw);
                    log.warn("Toss confirm failed: status={} code={} message={}",
                            response.getStatusCode(), err.code(), err.message());
                    throw new TossApiException(err.code(), err.message());
                })
                .body(TossConfirmResponse.class);
    }

    private TossErrorResponse parseError(String raw) {
        try {
            return MAPPER.readValue(raw, TossErrorResponse.class);
        } catch (Exception e) {
            return new TossErrorResponse("UNKNOWN", raw == null ? "" : raw);
        }
    }
}
