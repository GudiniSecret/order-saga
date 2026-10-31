# PROGRESS.md
Текущая фаза: 1 — Spring-база (order-service CRUD)

Сделано:
- Монорепо `order-saga/`: корневой `.gitignore`, `docker-compose.yml` (order-db: postgres:16 + healthcheck, порт 5434 на хосте)
- `order-service` сгенерирован: Spring Boot 4.1.2, Java 21, Maven, пакет `io.github.antonxnotna.order`
- `application.yml`: datasource, `ddl-auto: validate`, `open-in-view: false`, health show-details
- `/actuator/health` → UP, `db` UP (PostgreSQL)
- `./mvnw test`: `contextLoads` зелёный на Testcontainers (postgres:16)
- Решение: Spring Boot 4.1 вместо 3.5 (3.5 вне OSS-поддержки с 30.06.2026)
- Миграция `V1__init.sql` применена и закоммичена: `orders` (UUID PK, CHECK на status и total_amount, индекс `(customer_id, created_at DESC)`), `order_items` (IDENTITY PK, FK на orders ON DELETE CASCADE, UNIQUE (order_id, product_id), CHECK на quantity и price). FK на customers/products нет — database-per-service

- JPA-сущности `Order` (UUID через `GenerationType.UUID`, без сеттеров, `addItem` пересчитывает сумму), `OrderItem` (IDENTITY, LAZY `@ManyToOne`, owning side), `OrderStatus`, `OrderRepository` с `findByCustomerIdOrderByCreatedAtDesc`; `OrderRepositoryTest` (flush + clear) зелёный, 2/2 тестов

В работе:
- Шаг 4: DTO-records, `OrderService`, `POST /orders`, интеграционный тест на MockMvc
- `DECISIONS.md`: 001 структура репо, 002 Spring Boot 4.1, 003 идентификаторы и деньги, 004 цена приходит от клиента (упрощение MVP)

Открытые вопросы:
- Предупреждение Mockito о self-attaching агенте в тестах — отложено, на JDK 21 не мешает
- Дубликаты productId в одном заказе (UNIQUE в БД) — решить на шаге 4: валидация запроса или слияние позиций