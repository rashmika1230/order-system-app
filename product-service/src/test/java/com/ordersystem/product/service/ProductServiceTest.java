package com.ordersystem.product.service;

import com.ordersystem.product.dto.ProductResponse;
import com.ordersystem.product.dto.ReserveStockResponse;
import com.ordersystem.product.entity.Product;
import com.ordersystem.product.exception.InsufficientStockException;
import com.ordersystem.product.exception.ProductNotFoundException;
import com.ordersystem.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findById_returnsProduct_whenProductExists() {
        // arrange
        Product product = productWithId(1L, "Keyboard", new BigDecimal("15500.00"), 25);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // act
        ProductResponse response = productService.findById(1L);

        // assert
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Keyboard");
        assertThat(response.price()).isEqualByComparingTo("15500.00");
        assertThat(response.stock()).isEqualTo(25);
    }

    @Test
    void findById_throwsProductNotFoundException_whenProductIsMissing() {
        // arrange
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> productService.findById(99L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void reserveStock_returnsReservation_whenRepositoryUpdatesOneRow() {
        // arrange: the repository has already subtracted 3, so the reloaded product shows the new stock
        Product product = productWithId(1L, "Keyboard", new BigDecimal("15500.00"), 7);
        when(productRepository.reserveStock(1L, 3)).thenReturn(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // act
        ReserveStockResponse response = productService.reserveStock(1L, 3);

        // assert
        assertThat(response.productId()).isEqualTo(1L);
        assertThat(response.reservedQuantity()).isEqualTo(3);
        assertThat(response.unitPrice()).isEqualByComparingTo("15500.00");
        assertThat(response.remainingStock()).isEqualTo(7);
    }

    @Test
    void reserveStock_throwsInsufficientStockException_whenNoRowUpdatedAndProductExists() {
        // arrange
        Product product = productWithId(1L, "Keyboard", new BigDecimal("15500.00"), 2);
        when(productRepository.reserveStock(1L, 5)).thenReturn(0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // act
        InsufficientStockException ex = catchThrowableOfType(InsufficientStockException.class,
            () -> productService.reserveStock(1L, 5));

        // assert
        assertThat(ex).isNotNull();
        assertThat(ex.getProductId()).isEqualTo(1L);
        assertThat(ex.getRequestedQuantity()).isEqualTo(5);
        assertThat(ex.getAvailableStock()).isEqualTo(2);
    }

    @Test
    void reserveStock_throwsProductNotFoundException_whenNoRowUpdatedAndProductIsMissing() {
        // arrange
        when(productRepository.reserveStock(99L, 1)).thenReturn(0);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // act
        ProductNotFoundException ex = catchThrowableOfType(ProductNotFoundException.class,
            () -> productService.reserveStock(99L, 1));

        // assert
        assertThat(ex).isNotNull();
        assertThat(ex.getProductId()).isEqualTo(99L);
    }

    private static Product productWithId(Long id, String name, BigDecimal price, int stock) {
        Product product = new Product(name, "test product", price, stock);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
