package com.ordersystem.order.controller;

import com.ordersystem.order.dto.CreateOrderRequest;
import com.ordersystem.order.dto.OrderResponse;
import com.ordersystem.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @Valid @RequestBody
        CreateOrderRequest request){

        OrderResponse order = orderService.createOrder(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(order.id())
            .toUri();

        return ResponseEntity.created(uri).body(order);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.findById(id);
    }

}
