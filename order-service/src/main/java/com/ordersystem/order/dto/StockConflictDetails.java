package com.ordersystem.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockConflictDetails(
    Integer availableStock
) {
}
