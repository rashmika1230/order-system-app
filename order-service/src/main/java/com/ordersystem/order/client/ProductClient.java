package com.ordersystem.order.client;

import com.ordersystem.order.client.dto.ReserveStockRequest;
import com.ordersystem.order.client.dto.ReserveStockResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/products")
public interface ProductClient {

    @PostExchange("/{id}/reserve")
    ReserveStockResponse reserveStock(@PathVariable("id") Long productId,
                                      @RequestBody ReserveStockRequest request);
}
