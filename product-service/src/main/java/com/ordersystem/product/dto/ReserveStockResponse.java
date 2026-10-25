package com.ordersystem.product.dto;

import java.math.BigDecimal;

public record ReserveStockResponse(
    long productId,
    int reservedQuantity,
    BigDecimal unitPrice,
    Integer remainingStock
) {
}
