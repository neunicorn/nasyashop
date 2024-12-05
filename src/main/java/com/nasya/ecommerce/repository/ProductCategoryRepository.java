package com.nasya.ecommerce.repository;

import com.nasya.ecommerce.entity.ProductCategory;
import com.nasya.ecommerce.entity.ProductCategory.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
}
