package com.ordersystem.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReserveStockRequest(
    @NotNull(message = "Product ID is required")
    @Positive(message = "Quantity must be greater than 0")
    int quantity) {
}
