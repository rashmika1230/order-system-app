package com.ordersystem.product.exception;

public class ProductNotFoundException extends RuntimeException{

    private final long productId;

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
        this.productId = id;
    }

    public long getProductId() {
        return productId;
    }

}
