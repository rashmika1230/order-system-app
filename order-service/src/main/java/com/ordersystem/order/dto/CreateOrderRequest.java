package com.ordersystem.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be a positive number")
    Long productId,

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity must be at most 100")
    Integer quantity
) {
}
