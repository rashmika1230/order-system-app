package com.ordersystem.order.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockConflictDetails(
    Integer availableStock
) {
}
