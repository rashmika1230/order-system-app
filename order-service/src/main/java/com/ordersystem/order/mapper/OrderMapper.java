package com.ordersystem.order.mapper;

import com.ordersystem.order.dto.OrderResponse;
import com.ordersystem.order.entity.Order;

public final class OrderMapper {
    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order){
        return new OrderResponse(
            order.getId(),
            order.getProductId(),
            order.getQuantity(),
            order.getUnitPrice(),
            order.getTotalPrice(),
            order.getStatus(),
            order.getCreatedAt()
        );
    }
}
