package io.github.antonxnotna.order.service;

import io.github.antonxnotna.order.domain.Order;
import io.github.antonxnotna.order.dto.CreateOrderRequest;
import io.github.antonxnotna.order.dto.OrderItemRequest;
import io.github.antonxnotna.order.dto.OrderResponse;
import io.github.antonxnotna.order.exception.OrderNotFoundException;
import io.github.antonxnotna.order.messaging.OrderEventPublisher;
import io.github.antonxnotna.order.repository.OrderRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    private final Clock clock;

    // Autowired не нужен, конструктор один
    public OrderService(OrderRepository orderRepository, OrderEventPublisher eventPublisher, Clock clock) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(request.customerId(), clock.instant());
        for (OrderItemRequest item : request.items()) {
            order.addItem(item.productId(), item.quantity(), item.price());
        }

        Order saved = orderRepository.save(order);
        // Событие пишется в outbox в этой же транзакции
        // В Kafka его отправит OutboxRelay (at-least-once)
        eventPublisher.publishOrderCreated(saved);
        return OrderResponse.from(saved);
    }

    @Transactional (readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderResponse.from(order);
    }

    @Transactional (readOnly = true)
    public List<OrderResponse> getOrderByCustomer(UUID customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(OrderResponse::from) // order.getOrderItems.stream().. будет вызван и Hibernate пойдёт в БД
                .toList();
    }
}
