package io.github.antonxnotna.payment.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(UUID eventId, UUID orderId, Instant occurredAt, int version, Payload payload) {

    public static final String EVENT_TYPE = "OrderCreated";

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(UUID customerId, BigDecimal totalAmount) {
    }
}