package io.github.antonxnotna.order.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequest(
        @NotNull UUID productId,
        @NotNull @Positive Integer quantity, // Integer, чтобы по умолчанию был null, а не 0
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal price
        ) { }
