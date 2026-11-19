package io.github.antonxnotna.order.outbox;

import java.util.UUID;

public record OutboxMessage(
        UUID id,
        UUID aggregate_id,
        String topic,   // Куда отправляем (Kafka)
        String payload  // Что отправляем (EventEnvelope)
) { }
