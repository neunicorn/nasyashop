package com.nasya.ecommerce.service;

import com.nasya.ecommerce.model.request.product.ProductRequest;
import com.nasya.ecommerce.model.response.product.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> findAll();

    ProductResponse  findById(Long productId);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long productId, ProductRequest request);

    void delete(Long id);
}
