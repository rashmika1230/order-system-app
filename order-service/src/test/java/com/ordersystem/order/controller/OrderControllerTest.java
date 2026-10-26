package com.ordersystem.order.controller;

import com.ordersystem.order.dto.CreateOrderRequest;
import com.ordersystem.order.dto.OrderResponse;
import com.ordersystem.order.entity.OrderStatus;
import com.ordersystem.order.exception.ProductServiceUnavailableException;
import com.ordersystem.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_returns201WithLocation_whenRequestIsValid() throws Exception {
        // arrange
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(new OrderResponse(
            10L, 7L, 3, new BigDecimal("12.50"), new BigDecimal("37.50"), OrderStatus.CONFIRMED,
            Instant.parse("2026-10-26T10:15:30.123456Z")));

        // act + assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId": 7, "quantity": 3}
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/orders/10")))
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.totalPrice").value(37.50))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createOrder_returns400WithFieldErrors_whenProductIdMissingAndQuantityZero() throws Exception {
        // act + assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"quantity": 0}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors.productId").value("Product ID is required"))
            .andExpect(jsonPath("$.errors.quantity").value("Quantity must be at least 1"));

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrder_returns400_whenQuantityExceedsMaximum() throws Exception {
        // act + assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId": 7, "quantity": 101}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.quantity").value("Quantity must be at most 100"));

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrder_returns503WithoutInternalDetails_whenProductServiceIsUnavailable() throws Exception {
        // arrange: the real cause names the internal host and port of product-service
        ResourceAccessException cause = new ResourceAccessException(
            "I/O error on POST request for \"http://localhost:8081/api/products/7/reserve\": "
                + "Connection refused: localhost/127.0.0.1:8081",
            new ConnectException("Connection refused: localhost/127.0.0.1:8081"));
        when(orderService.createOrder(any(CreateOrderRequest.class)))
            .thenThrow(new ProductServiceUnavailableException("product-service call failed", cause));

        // act + assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId": 7, "quantity": 1}
                    """))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.title").value("Service unavailable"))
            .andExpect(content().string(not(containsString("localhost"))))
            .andExpect(content().string(not(containsString("8081"))));
    }
}
