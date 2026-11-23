package io.github.antonxnotna.order.outbox;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class OutboxRepository {
    private final JdbcClient jdbcClient;

    public OutboxRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void insert(OutboxMessage message) {
        jdbcClient.sql(
                """
                INSERT INTO outbox (id, aggregate_id, topic, payload)
                VALUES (:id, :aggregateId, :topic, CAST(:payload AS jsonb))
                """
        ).param("id", message.id())
                .param("aggregateId", message.aggregateId())
                .param("topic", message.topic())
                .param("payload", message.payload())
                .update();

    }

    public List<OutboxMessage> lockBatch(int limit) {
        return  jdbcClient.sql(
                """
                SELECT id, aggregate_id, topic, CAST(payload AS text) AS payload 
                FROM outbox
                WHERE published_at IS NULL
                ORDER BY created_at, id
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
                """)
                .param("limit", limit)
                .query((rs, rowNum) -> new OutboxMessage( // Преобразование каждой строки SQL в объект OutboxMessage
                        rs.getObject("id", UUID.class),
                        rs.getObject("aggregate_id", UUID.class),
                        rs.getString("topic"),
                        rs.getString("payload")
                )).list();
    }

    public int markPublished(UUID id) {
        return jdbcClient.sql("""
                UPDATE outbox
                SET published_at = NOW()
                WHERE id = :id
                  AND published_at IS NULL
                """)
                .param("id", id)
                .update();
    }
}
