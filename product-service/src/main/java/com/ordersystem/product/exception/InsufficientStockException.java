package com.ordersystem.product.exception;

public class InsufficientStockException extends RuntimeException{

    private final long productId;
    private final int requestedQuantity;
    private final int availableStock;

    public InsufficientStockException(long productId, int requestedQuantity, int availableStock) {
        super("Insufficient stock for product ID: " + productId + ", Requested quantity: " + requestedQuantity + ", Available stock: " + availableStock);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public long getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
