package com.ordersystem.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    String name,

    @Size(max = 500, message = "Description must be less than 500 characters")
    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0.01")
    @Digits(integer = 8, fraction = 2, message = "Price must have 2 digits after the decimal point")
    BigDecimal price,

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock Can not be negative")
    Integer stock
) {
}
