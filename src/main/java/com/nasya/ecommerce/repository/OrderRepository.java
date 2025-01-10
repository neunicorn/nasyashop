package com.nasya.ecommerce.repository;

import com.nasya.ecommerce.entity.Cart;
import com.nasya.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
