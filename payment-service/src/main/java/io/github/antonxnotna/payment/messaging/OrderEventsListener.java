package io.github.antonxnotna.payment.messaging;

import io.github.antonxnotna.payment.service.PaymentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrderEventsListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsListener.class);

    private final JsonMapper jsonMapper;
    private final PaymentService paymentService;

    public OrderEventsListener(JsonMapper jsonMapper, PaymentService paymentService) {
        this.jsonMapper = jsonMapper;
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order.events")
    public void onMessage(ConsumerRecord<String, String> record) {
        IncomingEventHeader header = jsonMapper.readValue(record.value(), IncomingEventHeader.class);
        log.info("Received {} for order {} (partition {}, offset {})",
                header.eventType(), header.orderId(), record.partition(), record.offset());

        switch (header.eventType()) {
            case OrderCreatedEvent.EVENT_TYPE ->
                    paymentService.handleOrderCreated(jsonMapper.readValue(record.value(), OrderCreatedEvent.class));
            default -> log.info("Skipping event type {}", header.eventType());
        }
    }
}