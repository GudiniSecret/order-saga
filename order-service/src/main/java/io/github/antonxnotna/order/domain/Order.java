package io.github.antonxnotna.order.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany (mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();


    protected Order() {
    }

    public Order(UUID customerId, Instant createdAt) {
        this.customerId = customerId;
        this.createdAt = createdAt;
        this.status = OrderStatus.NEW;
        this.totalAmount = BigDecimal.ZERO; // Пустой заказ
    }

    public void addItem(UUID product_id, int quantity, BigDecimal price) {
        OrderItem item = new OrderItem(this, product_id, quantity, price);
        orderItems.add(item);
        totalAmount = totalAmount.add(item.lineTotal());
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems); // Неизменяемый список, чтобы никто не сделал order.getItems().add(...)
    }
}
