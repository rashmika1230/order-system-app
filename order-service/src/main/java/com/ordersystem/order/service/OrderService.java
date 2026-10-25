package com.ordersystem.order.service;

import com.ordersystem.order.client.ProductClient;
import com.ordersystem.order.client.dto.ReserveStockRequest;
import com.ordersystem.order.client.dto.ReserveStockResponse;
import com.ordersystem.order.client.dto.StockConflictDetails;
import com.ordersystem.order.dto.CreateOrderRequest;
import com.ordersystem.order.dto.OrderResponse;
import com.ordersystem.order.entity.Order;
import com.ordersystem.order.entity.OrderStatus;
import com.ordersystem.order.exception.InsufficientStockException;
import com.ordersystem.order.exception.OrderNotFoundException;
import com.ordersystem.order.exception.ProductNotFoundException;
import com.ordersystem.order.exception.ProductServiceUnavailableException;
import com.ordersystem.order.mapper.OrderMapper;
import com.ordersystem.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {

        ReserveStockResponse reserveStock = reserveStock(request.productId(), request.quantity());

        Order order = new Order(
            request.productId(),
            request.quantity(),
            reserveStock.unitPrice(),
            OrderStatus.CONFIRMED
        );

        Order savedOrder = orderRepository.save(order);

        log.info("Order {} created: product {}, quantity {}, total {}",
            savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(), savedOrder.getTotalPrice());

        return OrderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return orderRepository.findById(id)
            .map(OrderMapper::toResponse)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private ReserveStockResponse reserveStock(Long productId, Integer quantity) {
        try {
            return productClient.reserveStock(productId, new ReserveStockRequest(quantity));
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductNotFoundException(productId);
        } catch (HttpClientErrorException.Conflict ex) {
            throw new InsufficientStockException(productId, quantity, readAvailableStock(ex));
        } catch (ResourceAccessException | HttpServerErrorException ex) {
            // ResourceAccessException = connection refused or timeout; HttpServerErrorException = 5xx
            throw new ProductServiceUnavailableException("product-service call failed", ex);
        }
    }

    private Integer readAvailableStock(HttpClientErrorException ex) {
        try {
            StockConflictDetails details = ex.getResponseBodyAs(StockConflictDetails.class);
            return details != null ? details.availableStock() : null;
        } catch (RuntimeException parseFailure) {
            log.debug("Could not read availableStock from 409 response", parseFailure);
            return null;
        }
    }
}
