package io.github.antonxnotna.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.*;

public record CreateOrderRequest (
        @NotNull UUID customerId,
        @NotEmpty @Size(max = 50) List<@NotNull @Valid OrderItemRequest> items
) {
    // Гарантирует, что при валидации результат isProductIdUnique() = True
    @AssertTrue(message = "No duplicates in productId")
    public boolean isProductIdsUnique() {
        if (items == null) {
            return true;
        } else {
            List<UUID> ids = items.stream().
                    filter(Objects::nonNull).
                    map(OrderItemRequest::productId).
                    filter(Objects::nonNull). // Поставил до этого isNull ;))...
                    toList();
            return ids.size() == new HashSet<>(ids).size();
        }
    }
}
