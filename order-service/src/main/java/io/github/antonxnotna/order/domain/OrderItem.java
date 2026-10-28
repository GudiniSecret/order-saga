package io.github.antonxnotna.order.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID product_id;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    protected OrderItem() {
    } // protected, чтобы нельзя было создать снаружи пустой заказ без покупателя
    // Пустой конструктор требует JPA для рефлексии

    // package-private, чтобы позицию можно было создать только через Order.addItem
    OrderItem(Order order, UUID product_id, int quantity, BigDecimal price) {
        this.order = order;
        this.product_id = product_id;
        this.quantity = quantity;
        this.price = price;
    }

    BigDecimal lineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public UUID getProductId() {
        return product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
