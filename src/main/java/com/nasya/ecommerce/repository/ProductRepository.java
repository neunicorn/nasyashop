package com.nasya.ecommerce.repository;

import com.nasya.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
    SELECT * FROM product
    WHERE LOWER("name") like :name
    """, nativeQuery=true)
    Page<Product> findByNamePageable(String name, Pageable pageable);


    @Query(value = """
    SELECT DISTINCT p.* FROM product p 
    JOIN product_category pc ON p.product_id = pc.product_id
    JOIN category c ON pc.category_id = c.category_id
    WHERE c.name = :categoryName
    """, nativeQuery=true)
    List<Product> findByCategory(@Param("categoryName") String categoryName);

    @Query(value = """
    SELECT * FROM product
    """, nativeQuery=true)
    Page<Product> findByPageable(Pageable pageable);
}
