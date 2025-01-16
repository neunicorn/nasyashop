package com.nasya.ecommerce.repository;

import com.nasya.ecommerce.entity.CartItem;
import com.nasya.ecommerce.entity.OrderItem;
import com.nasya.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query(value = """
    SELECT *.oi FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        JOIN product p ON o.product_id = p.id
        WHERE o.user_id = :user_id 
        AND oi.order_id = :order_id
    """, nativeQuery = true)
    List<OrderItem> findByUserAndProduct(Long userId, Product product);

    @Query(value = """
        SELECT SUM(quantity * price) FROM  order_items
                WHERE order_id = :orderId
        """, nativeQuery = true)
    Double calculateTotalOrder(Long orderId);
}
