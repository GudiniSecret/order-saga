package io.github.antonxnotna.order.messaging;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        UUID orderId,
        Instant occurredAt,
        int version,
        T payload
) {
    public static <T> EventEnvelope<T> of(String eventType, UUID orderId, Instant occurredAt, T payload) {
        return new EventEnvelope<>(UUID.randomUUID(), eventType, orderId, occurredAt, 1, payload);
    }
}
