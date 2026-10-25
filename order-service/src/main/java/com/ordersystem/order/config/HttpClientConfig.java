package com.ordersystem.order.config;

import com.ordersystem.order.client.ProductClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "product-service", types = ProductClient.class)
public class HttpClientConfig {
}
