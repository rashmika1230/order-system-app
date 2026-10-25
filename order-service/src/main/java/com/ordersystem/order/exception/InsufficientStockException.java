package com.ordersystem.order.exception;

public class InsufficientStockException extends RuntimeException{

    private final Long productId;
    private final Integer requestedQuantity;
    private final Integer availableStock;

    public InsufficientStockException(Long productId, Integer requestedQuantity, Integer availableStock) {
        super("Insufficient stock for product ID: " + productId + ", Requested quantity: " + requestedQuantity + ", Available stock: " + availableStock);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }
}
