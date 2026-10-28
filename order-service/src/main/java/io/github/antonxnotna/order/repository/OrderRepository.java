package io.github.antonxnotna.order.repository;

import io.github.antonxnotna.order.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.List;
import java.util.UUID;

// Реализацию для OrderRepository писать не нужно, при старте Spring Data создаёт прокси, который
// реализует этот интерфейс через SimpleJpaRepository, и регистрирует его как бин

// Методы save(), findById() и др. берутся оттуда

// Отдельного репозитория для OrderItem нет, позиции живут только внутри заказа и сохраняются каскадом
public interface OrderRepository extends JpaRepository<Order, UUID> {
    @EntityGraph(attributePaths = "orderItems")
    List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
