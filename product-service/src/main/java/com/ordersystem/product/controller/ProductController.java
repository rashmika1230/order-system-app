package com.ordersystem.product.controller;

import com.ordersystem.product.dto.CreateProductRequest;
import com.ordersystem.product.dto.ProductResponse;
import com.ordersystem.product.dto.ReserveStockRequest;
import com.ordersystem.product.dto.ReserveStockResponse;
import com.ordersystem.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> findAll(){
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id){
        return productService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody
        CreateProductRequest request){

        ProductResponse createdProduct = productService.create(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdProduct.id())
            .toUri();
        return ResponseEntity.created(uri).body(createdProduct);
    }

    @PostMapping("/{id}/reserve")
    public ReserveStockResponse reserveStock(
        @PathVariable Long id,
        @RequestBody ReserveStockRequest request){
        return productService.reserveStock(id, request.quantity());
    }
}
