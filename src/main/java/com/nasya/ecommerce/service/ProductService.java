package com.nasya.ecommerce.service;

import com.nasya.ecommerce.model.request.product.ProductRequest;
import com.nasya.ecommerce.model.response.product.PaginatedProductResponse;
import com.nasya.ecommerce.model.response.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    List<ProductResponse> findAll();

    Page<ProductResponse> findByPage(Pageable pageable);

    Page<ProductResponse> findByNameAndPageable(String name, Pageable pageable);

    ProductResponse  findById(Long productId);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long productId, ProductRequest request);

    void delete(Long id);

    PaginatedProductResponse convertProductPage(Page<ProductResponse> response);
}
