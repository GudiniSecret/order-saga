package io.github.antonxnotna.order.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private static final int BATCH_SIZE = 100;
    // Больше delivery.timeout.m, чтобы к этому моменту каждая отправка уже завершилась успехом или ошибкой
    private static final long SEND_TIMEOUT_SECONDS = 15;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TransactionTemplate transactionTemplate;

    public OutboxRelay(OutboxRepository outboxRepository,
                       KafkaTemplate<String, String> kafkaTemplate,
                       TransactionTemplate transactionTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedDelay = 500)
    public void relay() {
        try {
            Integer sent = transactionTemplate.execute(status -> publishBatch());
            if (sent != null && sent > 0) {
                log.info("Relayed {} outbox messages", sent);
            }
        } catch (RuntimeException ex) {
            log.warn("Outbox relay failed, batch will be retried: {}", ex.toString());
        }
    }

    private int publishBatch() {
        List<OutboxMessage> batch = outboxRepository.lockBatch(BATCH_SIZE);
        if (batch.isEmpty()) {
            return 0;
        }

        CompletableFuture<?>[] sends = batch.stream()
                .map(message -> kafkaTemplate.send(message.topic(), message.aggregateId().toString(), message.payload()))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(sends)
                .orTimeout(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .join();

        batch.forEach(message -> outboxRepository.markPublished(message.id()));
        return batch.size();
    }
}