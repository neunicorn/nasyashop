package com.nasya.ecommerce.service;

import com.nasya.ecommerce.entity.Order;
import com.nasya.ecommerce.model.request.checkout.CheckoutRequest;
import com.nasya.ecommerce.model.response.order.OrderItemResponse;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    Order checkout(CheckoutRequest request);

    Optional<Order> findOrderById(Long orderId);
    List<Order> findOrderByUserId(Long userId);

    List<Order> findOrdersByStatus(String status);

    void cancelOrder (Long orderId);

    List<OrderItemResponse> findOrderItemsByOrderId(Long orderId);

    void updateOrderStatus(Long orderId, String newStatus);

    Double calculateOrderTotal(Long orderId);
}
