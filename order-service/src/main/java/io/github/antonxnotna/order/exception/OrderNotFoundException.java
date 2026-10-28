package io.github.antonxnotna.order.exception;

import java.util.UUID;

// RuntimeException, чтобы транзакция откатывалась. Не надо будет писать throws в каждом методе по цепочке
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID orderId) {
        super("Order " + orderId + "not found");
    }
}
