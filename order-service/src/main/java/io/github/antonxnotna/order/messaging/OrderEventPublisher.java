package io.github.antonxnotna.order.messaging;

import io.github.antonxnotna.order.config.KafkaTopicConfig;
import io.github.antonxnotna.order.domain.Order;
import io.github.antonxnotna.order.outbox.OutboxMessage;
import io.github.antonxnotna.order.outbox.OutboxRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrderEventPublisher {

    private final OutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;

    public OrderEventPublisher(OutboxRepository outboxRepository, JsonMapper jsonMapper) {
        this.outboxRepository = outboxRepository;
        this.jsonMapper = jsonMapper;
    }


    @Transactional(propagation = Propagation.MANDATORY) // Propagation.MANDATORY - выполняется внутри уже открытой транзакции
                                                        // если у outbox будет своя транзакция, мы снова вернёмся к dual write
    public void publishOrderCreated(Order order) {
        EventEnvelope<OrderCreatedPayload> event = EventEnvelope.of(
                OrderCreatedPayload.EVENT_TYPE,
                order.getId(),
                order.getCreatedAt(),
                OrderCreatedPayload.from(order)
        );
        outboxRepository.insert(new OutboxMessage(
                event.eventId(),                     // id строки outbox = eventId конверта
                order.getId(),                       // станет ключом сообщения Kafka
                KafkaTopicConfig.ORDER_EVENTS_TOPIC,
                jsonMapper.writeValueAsString(event)
        ));
    }
}