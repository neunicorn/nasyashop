package com.nasya.ecommerce.controller;

import com.nasya.ecommerce.model.request.product.ProductRequest;
import com.nasya.ecommerce.model.response.product.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long productId){
        return ResponseEntity.ok(
                ProductResponse.builder()
                        .name("product " + productId)
                        .description("product description")
                        .price(BigDecimal.valueOf(7000))
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(
                List.of(
                        ProductResponse.builder()
                                .name("product 1")
                                .description("product description")
                                .price(BigDecimal.valueOf(7000))
                                .build(),
                        ProductResponse.builder()
                                .name("product 2")
                                .description("product description")
                                .price(BigDecimal.valueOf(7000))
                                .build()
                )
        );
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest req){
        return ResponseEntity.ok(
                ProductResponse.builder()
                        .name(req.getName())
                        .description(req.getDescription())
                        .price(req.getPrice())
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id")Long productId ,
            @RequestBody @Valid ProductRequest req){
        return ResponseEntity.ok(
                ProductResponse.builder()
                        .name(req.getName())
                        .description(req.getDescription())
                        .price(req.getPrice())
                        .build()
        );
    }

}
