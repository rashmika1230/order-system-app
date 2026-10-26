package com.ordersystem.product.controller;

import com.ordersystem.product.dto.CreateProductRequest;
import com.ordersystem.product.dto.ProductResponse;
import com.ordersystem.product.exception.InsufficientStockException;
import com.ordersystem.product.exception.ProductNotFoundException;
import com.ordersystem.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void findById_returns200WithProduct_whenProductExists() throws Exception {
        // arrange
        when(productService.findById(1L)).thenReturn(
            new ProductResponse(1L, "Keyboard", "Tenkeyless", new BigDecimal("15500.00"), 25));

        // act + assert
        mockMvc.perform(get("/api/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Keyboard"))
            .andExpect(jsonPath("$.description").value("Tenkeyless"))
            .andExpect(jsonPath("$.price").value(15500.00))
            .andExpect(jsonPath("$.stock").value(25));
    }

    @Test
    void findById_returns404ProblemDetail_whenProductIsMissing() throws Exception {
        // arrange
        when(productService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        // act + assert
        mockMvc.perform(get("/api/products/99"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Product not found"))
            .andExpect(jsonPath("$.productId").value(99));
    }

    @Test
    void createProduct_returns201WithLocation_whenRequestIsValid() throws Exception {
        // arrange
        when(productService.create(any(CreateProductRequest.class))).thenReturn(
            new ProductResponse(5L, "Webcam", "1080p", new BigDecimal("9900.00"), 12));

        // act + assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Webcam",
                      "description": "1080p",
                      "price": 9900.00,
                      "stock": 12
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/products/5")))
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.name").value("Webcam"));
    }

    @Test
    void createProduct_returns400WithFieldErrors_whenRequestIsInvalid() throws Exception {
        // act + assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "price": -1.00,
                      "stock": -1
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors.name").value("Name is required"))
            .andExpect(jsonPath("$.errors.price").value("Price must be at least 0.01"))
            .andExpect(jsonPath("$.errors.stock").value("Stock Can not be negative"));

        verifyNoInteractions(productService);
    }

    /**
     * Regression test: a negative quantity used to reach the repository, where
     * {@code stock = stock - (-5)} increased the stock instead of rejecting the request.
     * The {@code @Valid} on the reserve endpoint must stop it before the service is called.
     */
    @Test
    void reserveStock_returns400AndNeverCallsService_whenQuantityIsNegative() throws Exception {
        // act + assert
        mockMvc.perform(post("/api/products/1/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"quantity": -5}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.quantity").value("Quantity must be greater than 0"));

        verifyNoInteractions(productService);
    }

    @Test
    void reserveStock_returns409WithStockDetails_whenStockIsInsufficient() throws Exception {
        // arrange
        when(productService.reserveStock(1L, 10)).thenThrow(new InsufficientStockException(1L, 10, 3));

        // act + assert
        mockMvc.perform(post("/api/products/1/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"quantity": 10}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Insufficient stock"))
            .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.requestedQuantity").value(10))
            .andExpect(jsonPath("$.availableStock").value(3));
    }
}
