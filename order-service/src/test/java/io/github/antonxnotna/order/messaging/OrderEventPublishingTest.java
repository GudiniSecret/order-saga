package io.github.antonxnotna.order.messaging;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.github.antonxnotna.order.TestcontainersConfiguration;
import io.github.antonxnotna.order.config.KafkaTopicConfig;
import io.github.antonxnotna.order.dto.CreateOrderRequest;
import io.github.antonxnotna.order.dto.OrderItemRequest;
import io.github.antonxnotna.order.dto.OrderResponse;
import io.github.antonxnotna.order.service.OrderService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.kafka.KafkaContainer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OrderEventPublishingTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private KafkaContainer kafkaContainer;

    @Test
    void publishesOrderCreatedWithOrderIdAsKey() {
        UUID customerId = UUID.randomUUID();
        OrderResponse order = orderService.createOrder(new CreateOrderRequest(customerId, List.of(
                new OrderItemRequest(UUID.randomUUID(), 2, new BigDecimal("100.50")),
                new OrderItemRequest(UUID.randomUUID(), 1, new BigDecimal("49.00"))
        )));

        ConsumerRecord<String, String> record = pollForKey(order.id().toString(), Duration.ofSeconds(10));

        DocumentContext event = JsonPath.parse(record.value());
        assertThat(event.read("$.eventType", String.class)).isEqualTo("OrderCreated");
        assertThat(event.read("$.orderId", String.class)).isEqualTo(order.id().toString());
        assertThat(event.read("$.eventId", String.class)).isNotBlank();
        assertThat(event.read("$.version", Integer.class)).isEqualTo(1);
        assertThat(Instant.parse(event.read("$.occurredAt", String.class))).isNotNull();
        assertThat(event.read("$.payload.customerId", String.class)).isEqualTo(customerId.toString());
        assertThat(event.read("$.payload.items.length()", Integer.class)).isEqualTo(2);
    }

    private ConsumerRecord<String, String> pollForKey(String key, Duration timeout) {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(KafkaTopicConfig.ORDER_EVENTS_TOPIC));
            long deadline = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    if (key.equals(record.key())) {
                        return record;
                    }
                }
            }
        }
        throw new AssertionError("No event with key " + key + " within " + timeout);
    }
}