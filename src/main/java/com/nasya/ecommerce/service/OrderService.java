package com.nasya.ecommerce.service;

import com.nasya.ecommerce.entity.Order;
import com.nasya.ecommerce.model.OrderStatus;
import com.nasya.ecommerce.model.request.checkout.CheckoutRequest;
import com.nasya.ecommerce.model.response.order.OrderItemResponse;
import com.nasya.ecommerce.model.response.order.OrderResponse;
import com.nasya.ecommerce.model.response.order.PaginatedOrderResponse;
import com.nasya.ecommerce.model.response.product.PaginatedProductResponse;
import com.nasya.ecommerce.model.response.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderResponse checkout(CheckoutRequest request);

    Optional<Order> findOrderById(Long orderId);

    List<Order> findOrderByUserId(Long userId);

    Page<OrderResponse> findOrderByUserIdAndPageable(Long userId, Pageable pageable);

    List<Order> findOrdersByStatus(OrderStatus status);

    void cancelOrder (Long orderId);

    List<OrderItemResponse> findOrderItemsByOrderId(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus newStatus);

    Double calculateOrderTotal(Long orderId);

    PaginatedOrderResponse convertProductPage(Page<OrderResponse> response);
}
