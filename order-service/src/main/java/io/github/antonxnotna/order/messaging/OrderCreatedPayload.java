package io.github.antonxnotna.order.messaging;

import io.github.antonxnotna.order.domain.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedPayload(UUID customerId,
                                  BigDecimal totalAmount,
                                  List<Item> items) {

    public static final String EVENT_TYPE = "OrderCreated";

    public record Item(UUID productId, int quantity, BigDecimal price) {
    }

    public static OrderCreatedPayload from(Order order) {
        return new OrderCreatedPayload(
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getOrderItems().stream()
                        .map(item -> new Item(item.getProductId(), item.getQuantity(), item.getPrice()))
                        .toList()
        );
    }
}