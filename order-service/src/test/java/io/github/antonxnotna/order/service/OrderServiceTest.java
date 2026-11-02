package io.github.antonxnotna.order.service;

import io.github.antonxnotna.order.domain.Order;
import io.github.antonxnotna.order.domain.OrderStatus;
import io.github.antonxnotna.order.dto.CreateOrderRequest;
import io.github.antonxnotna.order.dto.OrderItemRequest;
import io.github.antonxnotna.order.dto.OrderResponse;
import io.github.antonxnotna.order.exception.OrderNotFoundException;
import io.github.antonxnotna.order.messaging.OrderEventPublisher;
import io.github.antonxnotna.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-28T10:00:00Z");

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsNewOrderWithCalculatedTotalAndCurrentTime() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UUID customerId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest(customerId, List.of(
                new OrderItemRequest(UUID.randomUUID(), 2, new BigDecimal("100.50")),
                new OrderItemRequest(UUID.randomUUID(), 1, new BigDecimal("49.00"))
        ));

        OrderResponse response = orderService.createOrder(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        assertThat(saved.getCustomerId()).isEqualTo(customerId);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("250.00");
        assertThat(response.items()).hasSize(2);
        verify(eventPublisher).publishOrderCreated(saved);
    }

    @Test
    void throwsWhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(orderId.toString());
    }
}
