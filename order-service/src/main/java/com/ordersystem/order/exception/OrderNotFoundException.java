package com.ordersystem.order.exception;

public class OrderNotFoundException extends RuntimeException{

    private final Long orderId;

    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id + " was not found");
        this.orderId = id;
    }

    public Long getOrderId() {
        return orderId;
    }



}
