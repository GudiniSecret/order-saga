package io.github.antonxnotna.order.repository;

import io.github.antonxnotna.order.domain.Order;
import io.github.antonxnotna.order.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import io.github.antonxnotna.order.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesOrderWithItemsAndLoadsItBack() {
        UUID customerId = UUID.randomUUID();
        Order order = new Order(customerId, Instant.now());
        order.addItem(UUID.randomUUID(), 2, new BigDecimal("100.50"));
        order.addItem(UUID.randomUUID(), 1, new BigDecimal("49.00"));

        // 3 следующие команды нужны для корректности проверки:

        // Сохранить объект в контексте Jpa (кэш первого уровня). Order передаётся под управление Hibernate
        orderRepository.save(order);

        // Синхронизация изменений из памяти с БД. Hibernate принудительно выполняет все накопившиеся SQL-команды
        entityManager.flush();

        // Чистим кэш первого уровня. Без этого orderRepository.findById() просто возьмёт объект из кэша без обращения к БД
        entityManager.clear();

        Order loaded = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(loaded.getCustomerId()).isEqualTo(customerId);
        assertThat(loaded.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(loaded.getTotalAmount()).isEqualByComparingTo("250.00");
        assertThat(loaded.getOrderItems())
                .hasSize(2)
                .allSatisfy(item -> assertThat(item.getId()).isNotNull());

    }
}
