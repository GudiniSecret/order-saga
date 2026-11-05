package io.github.antonxnotna.payment.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IncomingEventHeader(UUID eventId, String eventType, UUID orderId, int version) {
}