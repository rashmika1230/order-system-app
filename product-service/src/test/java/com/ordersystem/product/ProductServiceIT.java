package com.ordersystem.product;

import com.ordersystem.product.dto.CreateProductRequest;
import com.ordersystem.product.dto.ProductResponse;
import com.ordersystem.product.dto.ReserveStockResponse;
import com.ordersystem.product.exception.InsufficientStockException;
import com.ordersystem.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ProductServiceIT {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Autowired
    private ProductService productService;

    @Test
    void findAll_returnsSeedProducts_whenFlywayMigrationsHaveRun() {
        // act
        List<ProductResponse> products = productService.findAll();

        // assert
        assertThat(products).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    void reserveStock_reducesStockInDatabase_whenStockIsAvailable() {
        // arrange
        ProductResponse product = productService.create(
            new CreateProductRequest("IT reserve product", "created by test", new BigDecimal("250.00"), 5));

        // act
        ReserveStockResponse reservation = productService.reserveStock(product.id(), 2);

        // assert
        assertThat(reservation.reservedQuantity()).isEqualTo(2);
        assertThat(reservation.remainingStock()).isEqualTo(3);
        assertThat(reservation.unitPrice()).isEqualByComparingTo("250.00");
        assertThat(productService.findById(product.id()).stock()).isEqualTo(3);
    }

    @Test
    void reserveStock_allowsExactlyOneReservation_whenTenThreadsCompeteForLastUnit() throws Exception {
        // arrange
        int threads = 10;
        ProductResponse product = productService.create(
            new CreateProductRequest("IT concurrency product", "created by test", new BigDecimal("99.00"), 1));
        Long productId = product.id();

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<ReserveStockResponse>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return productService.reserveStock(productId, 1);
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();

            // act: release all threads at the same moment
            start.countDown();

            int succeeded = 0;
            int insufficient = 0;
            List<Throwable> unexpected = new ArrayList<>();
            for (Future<ReserveStockResponse> future : futures) {
                try {
                    future.get(30, TimeUnit.SECONDS);
                    succeeded++;
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof InsufficientStockException) {
                        insufficient++;
                    } else {
                        unexpected.add(ex.getCause());
                    }
                }
            }

            // assert
            assertThat(unexpected).isEmpty();
            assertThat(succeeded).isEqualTo(1);
            assertThat(insufficient).isEqualTo(threads - 1);
            assertThat(productService.findById(productId).stock()).isZero();
        } finally {
            executor.shutdownNow();
        }
    }
}
