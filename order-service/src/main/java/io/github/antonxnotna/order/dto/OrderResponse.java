package io.github.antonxnotna.order.dto;

import io.github.antonxnotna.order.domain.Order;
import io.github.antonxnotna.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse (
        UUID id,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getOrderItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
