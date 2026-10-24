package com.ordersystem.product.service;

import com.ordersystem.product.dto.CreateProductRequest;
import com.ordersystem.product.dto.ProductResponse;
import com.ordersystem.product.dto.ReserveStockRequest;
import com.ordersystem.product.dto.ReserveStockResponse;
import com.ordersystem.product.entity.Product;
import com.ordersystem.product.exception.InsufficientStockException;
import com.ordersystem.product.exception.ProductNotFoundException;
import com.ordersystem.product.mapper.ProductMapper;
import com.ordersystem.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll(){

//        List<Product> products = productRepository.findAll();
//        List<ProductResponse> responseList = products.stream().map(ProductMapper::toResponse).toList();
//        return responseList;

        return productRepository.findAll()
            .stream()
            .map(ProductMapper::toResponse)
            .toList();
    }

    public ProductResponse findById(Long id){
        return productRepository.findById(id)
            .map(ProductMapper::toResponse)
            .orElseThrow(() ->new ProductNotFoundException(id));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest createProductRequest){
        Product savedProduct = productRepository.save(ProductMapper.toEntity(createProductRequest));
        return ProductMapper.toResponse(savedProduct);
    }

    @Transactional
    public ReserveStockResponse reserveStock(Long productId, int quantity){
        int updatedRows = productRepository.reserveStock(productId, quantity);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

        if(updatedRows == 0){
            throw new InsufficientStockException(productId, quantity, product.getStock());
        }

        return new ReserveStockResponse(
            product.getId(),
            quantity,
            product.getPrice(),
            product.getStock()
        );


    }
}
