package com.ordersystem.order;

import com.ordersystem.order.client.ProductClient;
import com.ordersystem.order.client.dto.ReserveStockRequest;
import com.ordersystem.order.client.dto.ReserveStockResponse;
import com.ordersystem.order.dto.CreateOrderRequest;
import com.ordersystem.order.dto.OrderResponse;
import com.ordersystem.order.entity.OrderStatus;
import com.ordersystem.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class OrderServiceIT {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @MockitoBean
    private ProductClient productClient;

    @Autowired
    private OrderService orderService;

    @Test
    void findById_returnsSavedOrder_whenOrderWasCreated() {
        // arrange
        when(productClient.reserveStock(eq(5L), any(ReserveStockRequest.class)))
            .thenReturn(new ReserveStockResponse(5L, 2, new BigDecimal("19.99"), 8));
        OrderResponse created = orderService.createOrder(new CreateOrderRequest(5L, 2));

        // act
        OrderResponse found = orderService.findById(created.id());

        // assert
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.productId()).isEqualTo(5L);
        assertThat(found.quantity()).isEqualTo(2);
        assertThat(found.unitPrice()).isEqualByComparingTo("19.99");
        assertThat(found.totalPrice()).isEqualByComparingTo("39.98");
        assertThat(found.status()).isEqualTo(OrderStatus.CONFIRMED);
        // Instant must survive the round trip through TIMESTAMP(6) unchanged
        assertThat(found.createdAt()).isEqualTo(created.createdAt());
    }
}
