package com.nasya.ecommerce.controller;

import com.nasya.ecommerce.model.request.product.ProductRequest;
import com.nasya.ecommerce.model.response.product.PaginatedProductResponse;
import com.nasya.ecommerce.model.response.product.ProductResponse;
import com.nasya.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
@SecurityRequirement(name = "Bearer")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long productId){

        ProductResponse response = productService.findById(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedProductResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "product_id,asc") String[] sort,
            @RequestParam(required = false) String name
    ){
        // implement multi sort
        // ex: sort asc by product name, desc by price product
        List<Sort.Order> orders = new ArrayList<>();
        if(sort[0].contains(",")){
            for(String sortOrder:sort){
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]),_sort[0]));
            }
        }else {
            orders.add(new Sort.Order(getSortDirection(sort[1]),sort[0]));
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<ProductResponse> response;

        if(name != null && !name.isEmpty()){
            response = productService.findByNameAndPageable(name, pageable);
        }else {
            response = productService.findByPage(pageable);
        }
        return ResponseEntity.ok(productService.convertProductPage(response));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest req){
        ProductResponse response = productService.create(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id")Long productId ,
            @RequestBody @Valid ProductRequest req){
        ProductResponse response = productService.update(productId, req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id")Long productId){
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

    private Sort.Direction getSortDirection(String direction){
        if(direction.equals("asc")){
            return Sort.Direction.ASC;
        } else if(direction.equals("desc")){
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

}
