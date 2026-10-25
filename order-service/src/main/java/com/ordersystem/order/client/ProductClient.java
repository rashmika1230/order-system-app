package com.ordersystem.order.client;

import com.ordersystem.order.dto.ReserveStockRequest;
import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/products")
public interface ProductClient {

    ReserveStockRequest reserveStock(

        @PathParam("id")
        Long productId,
        @RequestBody
        ReserveStockRequest request);
}
