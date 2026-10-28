package io.github.antonxnotna.order.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
public class OrderControllerTest {
    @Autowired
    private WebApplicationContext context;

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
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEmptyItems() throws Exception {
        String body = """
                {"customerId": "11111111-1111-1111-1111-111111111111", "items": []}
                """;

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
