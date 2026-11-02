package io.github.antonxnotna.order.messaging;

import io.github.antonxnotna.order.config.KafkaTopicConfig;
import io.github.antonxnotna.order.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    public OrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate, JsonMapper jsonMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    public void publishOrderCreated(Order order) {
        EventEnvelope<OrderCreatedPayload> event = EventEnvelope.of(
                OrderCreatedPayload.EVENT_TYPE,
                order.getId(),
                order.getCreatedAt(),
                OrderCreatedPayload.from(order)
        );
        String key = order.getId().toString(); // key явно, чтобы события шли в одну партицию
        String value = jsonMapper.writeValueAsString(event);

        kafkaTemplate.send(KafkaTopicConfig.ORDER_EVENTS_TOPIC, key, value)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} for order {}", event.eventType(), key, ex);
                    } else {
                        log.info("Published {} for order {} to partition {} offset {}",
                                event.eventType(), key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}