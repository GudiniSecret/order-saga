# order-saga

   Event-driven обработка заказов на микросервисах: сага поверх Kafka
   с transactional outbox и идемпотентными консьюмерами.

   **Статус:** Готов `order-service` (REST + PostgreSQL + Flyway),
   следующий этап - интеграция с Kafka.

   ## Стек
   Java 21, Spring Boot 4.1, PostgreSQL 16, Flyway, Kafka, Testcontainers, Docker Compose

   ## Запуск
   ```bash
   docker compose up -d
   cd order-service
   ./mvnw spring-boot:run
   ```

   ## Решения
   Архитектурные решения и альтернативы — в [DECISIONS.md](DECISIONS.md).