package com.ordersystem.order.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReserveStockResponse(
    Long productId,
    Integer reservedQuantity,
    BigDecimal unitPrice,
    Integer remainingStock

) {
}
