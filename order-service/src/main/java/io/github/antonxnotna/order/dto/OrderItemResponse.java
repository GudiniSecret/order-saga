package io.github.antonxnotna.order.dto;

import io.github.antonxnotna.order.domain.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

// int, а не Integer, потому что значение гарантировано и проверяется CHECK на уровне БД
public record OrderItemResponse (UUID productId, int quantity, BigDecimal price) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getPrice());
    }
}
