package com.ordersystem.order.dto;

import com.ordersystem.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
    Long id,
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice,
    OrderStatus status,
    Instant createdAt
    ) {
}
