package com.nasya.ecommerce.service;

import com.nasya.ecommerce.entity.Order;
import com.nasya.ecommerce.model.OrderStatus;
import com.nasya.ecommerce.model.request.checkout.CheckoutRequest;
import com.nasya.ecommerce.model.response.order.OrderItemResponse;
import com.nasya.ecommerce.model.response.order.OrderResponse;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderResponse checkout(CheckoutRequest request);

    Optional<Order> findOrderById(Long orderId);
    List<Order> findOrderByUserId(Long userId);

    List<Order> findOrdersByStatus(OrderStatus status);

    void cancelOrder (Long orderId);

    List<OrderItemResponse> findOrderItemsByOrderId(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus newStatus);

    Double calculateOrderTotal(Long orderId);
}
