package com.ordersystem.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

/**
 * Both backends point at localhost:1, where nothing listens. That lets the status code
 * tell us whether the gateway routed the request:
 * <ul>
 *   <li>404 = no route matched, so the gateway never tried to call a backend.</li>
 *   <li>5xx = a route matched and the gateway tried to forward the request,
 *       but the backend was unreachable.</li>
 * </ul>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "PRODUCT_SERVICE_URL=http://localhost:1",
        "ORDER_SERVICE_URL=http://localhost:1"
    })
class GatewayRoutesTest {

    @Value("${local.server.port}")
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .responseTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Test
    void reserveStock_returns404_becauseReserveEndpointIsPrivate() {
        // act + assert
        webTestClient.post().uri("/api/products/1/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {"quantity": 1}
                """)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void unknownPath_returns404_whenNoRouteIsDefined() {
        // act + assert
        webTestClient.get().uri("/api/customers")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void getProduct_returns5xx_whenRouteMatchesButBackendIsUnreachable() {
        // act + assert
        webTestClient.get().uri("/api/products/1")
            .exchange()
            .expectStatus().is5xxServerError();
    }

    @Test
    void getOrder_returns5xx_whenRouteMatchesButBackendIsUnreachable() {
        // act + assert
        webTestClient.get().uri("/api/orders/1")
            .exchange()
            .expectStatus().is5xxServerError();
    }
}
