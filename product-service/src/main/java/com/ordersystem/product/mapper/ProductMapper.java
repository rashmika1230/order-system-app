package com.ordersystem.product.mapper;

import com.ordersystem.product.dto.CreateProductRequest;
import com.ordersystem.product.dto.ProductResponse;
import com.ordersystem.product.entity.Product;

public final class ProductMapper {
    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product){
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock()
        );
    }

    public static Product toEntity(CreateProductRequest request){
        return new Product(
            request.name(),
            request.description(),
            request.price(),
            request.stock()
        );
    }
}
