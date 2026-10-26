package com.ordersystem.order.service;

import com.ordersystem.order.client.ProductClient;
import com.ordersystem.order.client.dto.ReserveStockRequest;
import com.ordersystem.order.client.dto.ReserveStockResponse;
import com.ordersystem.order.dto.CreateOrderRequest;
import com.ordersystem.order.dto.OrderResponse;
import com.ordersystem.order.entity.Order;
import com.ordersystem.order.entity.OrderStatus;
import com.ordersystem.order.exception.InsufficientStockException;
import com.ordersystem.order.exception.OrderNotFoundException;
import com.ordersystem.order.exception.ProductNotFoundException;
import com.ordersystem.order.exception.ProductServiceUnavailableException;
import com.ordersystem.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_savesConfirmedOrderWithPriceSnapshot_whenStockIsReserved() {
        // arrange
        when(productClient.reserveStock(eq(7L), any(ReserveStockRequest.class)))
            .thenReturn(new ReserveStockResponse(7L, 3, new BigDecimal("12.50"), 17));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // act
        OrderResponse response = orderService.createOrder(new CreateOrderRequest(7L, 3));

        // assert
        ArgumentCaptor<ReserveStockRequest> reserveCaptor = ArgumentCaptor.forClass(ReserveStockRequest.class);
        verify(productClient).reserveStock(eq(7L), reserveCaptor.capture());
        assertThat(reserveCaptor.getValue().quantity()).isEqualTo(3);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getProductId()).isEqualTo(7L);
        assertThat(savedOrder.getQuantity()).isEqualTo(3);
        assertThat(savedOrder.getUnitPrice()).isEqualByComparingTo("12.50");
        assertThat(savedOrder.getTotalPrice()).isEqualByComparingTo("37.50");
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        assertThat(response.unitPrice()).isEqualByComparingTo("12.50");
        assertThat(response.totalPrice()).isEqualByComparingTo("37.50");
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void createOrder_throwsProductNotFoundException_whenProductServiceReturns404() {
        // arrange
        when(productClient.reserveStock(eq(99L), any(ReserveStockRequest.class))).thenThrow(
            HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", new HttpHeaders(), new byte[0], UTF_8));

        // act
        Throwable thrown = catchThrowable(() -> orderService.createOrder(new CreateOrderRequest(99L, 1)));

        // assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException.class);
        assertThat(((ProductNotFoundException) thrown).getProductId()).isEqualTo(99L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_throwsInsufficientStockException_whenProductServiceReturns409() {
        // arrange
        when(productClient.reserveStock(eq(7L), any(ReserveStockRequest.class))).thenThrow(
            HttpClientErrorException.create(HttpStatus.CONFLICT, "Conflict", new HttpHeaders(), new byte[0], UTF_8));

        // act
        Throwable thrown = catchThrowable(() -> orderService.createOrder(new CreateOrderRequest(7L, 5)));

        // assert
        assertThat(thrown).isInstanceOf(InsufficientStockException.class);
        InsufficientStockException ex = (InsufficientStockException) thrown;
        assertThat(ex.getProductId()).isEqualTo(7L);
        assertThat(ex.getRequestedQuantity()).isEqualTo(5);
        // the empty 409 body cannot be read, so availableStock is left out (best-effort read)
        assertThat(ex.getAvailableStock()).isNull();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_throwsProductServiceUnavailableException_whenProductServiceIsUnreachable() {
        // arrange
        ResourceAccessException connectionRefused =
            new ResourceAccessException("I/O error on POST request", new ConnectException("Connection refused"));
        when(productClient.reserveStock(eq(7L), any(ReserveStockRequest.class))).thenThrow(connectionRefused);

        // act
        Throwable thrown = catchThrowable(() -> orderService.createOrder(new CreateOrderRequest(7L, 1)));

        // assert
        assertThat(thrown).isInstanceOf(ProductServiceUnavailableException.class);
        assertThat(thrown.getCause()).isSameAs(connectionRefused);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_throwsProductServiceUnavailableException_whenProductServiceReturns5xx() {
        // arrange
        when(productClient.reserveStock(eq(7L), any(ReserveStockRequest.class))).thenThrow(
            HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                new HttpHeaders(), new byte[0], UTF_8));

        // act
        Throwable thrown = catchThrowable(() -> orderService.createOrder(new CreateOrderRequest(7L, 1)));

        // assert
        assertThat(thrown).isInstanceOf(ProductServiceUnavailableException.class);
        assertThat(thrown.getCause()).isInstanceOf(HttpServerErrorException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void findById_throwsOrderNotFoundException_whenOrderIsMissing() {
        // arrange
        when(orderRepository.findById(42L)).thenReturn(Optional.empty());

        // act
        Throwable thrown = catchThrowable(() -> orderService.findById(42L));

        // assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException.class);
        assertThat(((OrderNotFoundException) thrown).getOrderId()).isEqualTo(42L);
    }
}
