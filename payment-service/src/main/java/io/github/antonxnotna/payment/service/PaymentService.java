package io.github.antonxnotna.payment.service;

import io.github.antonxnotna.payment.messaging.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Payment requested: order={}, customer={}, amount={}",
                event.orderId(), event.payload().customerId(), event.payload().totalAmount());
    }
}