
package io.github.antonxnotna.order.outbox;

import io.github.antonxnotna.order.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class OutboxRepositoryTest {

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void insertsLocksAndMarksEventAsPublished() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OutboxMessage message = new OutboxMessage(
                eventId,
                orderId,
                "order.events",
                "{\"test\":true}"
        );

        outboxRepository.insert(message);

        List<OutboxMessage> batch =
                outboxRepository.lockBatch(10);

        assertThat(batch)
                .extracting(OutboxMessage::id)
                .contains(eventId);

        OutboxMessage loaded = batch.stream()
                .filter(m -> m.id().equals(eventId))
                .findFirst()
                .orElseThrow();

        assertThat(loaded.aggregateId()).isEqualTo(orderId);
        assertThat(loaded.topic()).isEqualTo("order.events");
        assertThat(loaded.payload()).contains("\"test\"");

        // Помечаем событие опубликованным
        assertThat(outboxRepository.markPublished(eventId))
                .isEqualTo(1);

        // Оно больше не должно выбираться
        assertThat(outboxRepository.lockBatch(10))
                .extracting(OutboxMessage::id)
                .doesNotContain(eventId);
    }
}
