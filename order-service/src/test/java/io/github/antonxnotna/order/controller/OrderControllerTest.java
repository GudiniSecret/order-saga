package io.github.antonxnotna.order.controller;

import io.github.antonxnotna.order.domain.Order;
import io.github.antonxnotna.order.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import io.github.antonxnotna.order.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public class OrderControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createOrder() throws Exception {
        String body = """
                {
                  "customerId": "11111111-1111-1111-1111-111111111111",
                  "items": [
                    {"productId": "22222222-2222-2222-2222-222222222222", "quantity": 2, "price": 100.50},
                    {"productId": "33333333-3333-3333-3333-333333333333", "quantity": 1, "price": 49.00}
                  ]
                }
                """;
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void rejectsDuplicateProducts() throws Exception {
        String body = """
                {
                  "customerId": "11111111-1111-1111-1111-111111111111",
                  "items": [
                    {"productId": "22222222-2222-2222-2222-222222222222", "quantity": 1, "price": 10.00},
                    {"productId": "22222222-2222-2222-2222-222222222222", "quantity": 3, "price": 10.00}
                  ]
                }
                """;

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.productIdsUnique").exists());
    }

    @Test
    void rejectsEmptyItems() throws Exception {
        String body = """
                {"customerId": "11111111-1111-1111-1111-111111111111", "items": []}
                """;

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsOrderById() throws Exception {
        Order order = saveOrder(UUID.randomUUID(), Instant.parse("2026-10-01T10:00:00Z"));
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId().toString()))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void returns404ForUnknownOrder() throws Exception {
        mockMvc.perform(get("/orders/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"));
    }

    @Test
    void listsCustomerOrdersNewestFirst() throws Exception {
        UUID customerId = UUID.randomUUID();
        Order older = saveOrder(customerId, Instant.parse("2026-10-01T10:00:00Z"));
        Order newer = saveOrder(customerId, Instant.parse("2026-10-02T10:00:00Z"));
        saveOrder(UUID.randomUUID(), Instant.parse("2026-10-03T10:00:00Z")); // заказ другого покупателя
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/orders").param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(newer.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(older.getId().toString()));
    }

    private Order saveOrder(UUID customerId, Instant createdAt) {
        Order order = new Order(customerId, createdAt);
        order.addItem(UUID.randomUUID(), 1, new BigDecimal("10.00"));
        return orderRepository.save(order);
    }
}
