package io.github.antonxnotna.payment.messaging;

import io.github.antonxnotna.payment.TestcontainersConfiguration;
import io.github.antonxnotna.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OrderEventsListenerTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void passesOrderCreatedToPaymentServiceAndSkipsUnknownTypes() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        // одинаковый ключ → одна партиция → неизвестное событие гарантированно обработается первым
        kafkaTemplate.send("order.events", orderId.toString(), event("OrderShipped", orderId, customerId));
        kafkaTemplate.send("order.events", orderId.toString(), event("OrderCreated", orderId, customerId));

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(paymentService, timeout(15_000).times(1)).handleOrderCreated(captor.capture());

        OrderCreatedEvent received = captor.getValue();
        assertThat(received.orderId()).isEqualTo(orderId);
        assertThat(received.payload().customerId()).isEqualTo(customerId);
        assertThat(received.payload().totalAmount()).isEqualByComparingTo("250.00");
    }

    private static String event(String eventType, UUID orderId, UUID customerId) {
        return """
                {
                  "eventId": "%s",
                  "eventType": "%s",
                  "orderId": "%s",
                  "occurredAt": "2026-11-01T10:00:00Z",
                  "version": 1,
                  "payload": {
                    "customerId": "%s",
                    "totalAmount": 250.00,
                    "items": [{"productId": "%s", "quantity": 2, "price": 125.00}]
                  }
                }
                """.formatted(UUID.randomUUID(), eventType, orderId, customerId, UUID.randomUUID());
    }
}