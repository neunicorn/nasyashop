package com.nasya.ecommerce.controller;

import com.nasya.ecommerce.common.PageUtil;
import com.nasya.ecommerce.common.erros.BadRequestException;
import com.nasya.ecommerce.entity.Order;
import com.nasya.ecommerce.model.OrderStatus;
import com.nasya.ecommerce.model.request.checkout.CheckoutRequest;
import com.nasya.ecommerce.model.response.order.OrderItemResponse;
import com.nasya.ecommerce.model.response.order.OrderResponse;
import com.nasya.ecommerce.model.response.order.PaginatedOrderResponse;
import com.nasya.ecommerce.model.response.product.ProductResponse;
import com.nasya.ecommerce.security.UserInfo;
import com.nasya.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
@SecurityRequirement(name = "Bearer")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping(value = "/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo user = (UserInfo) auth.getPrincipal();
        request.setUserId(user.getUser().getUserId());

        OrderResponse order = orderService.checkout(request);
        return ResponseEntity.ok(order);
    };

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable Long orderId ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo user = (UserInfo) auth.getPrincipal();

        return orderService.findOrderById(orderId)
                .map(order -> {
                    if(order.getUserId().equals(user.getUser().getUserId())){
                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(OrderResponse.builder().build());
                    }
                    OrderResponse res = OrderResponse.fromOrder(order);
                    return ResponseEntity.ok(res);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping
    public ResponseEntity<PaginatedOrderResponse> findOrderByUserId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "order_id,desc") String[] sort
    ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo user = (UserInfo) auth.getPrincipal();

        List<Sort.Order> sortOrder = PageUtil.parseSortOrderRequest(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder));

        Page<OrderResponse> userOrders = orderService.findOrderByUserIdAndPageable(user.getUser().getUserId(), pageable);
        return ResponseEntity.ok(orderService.convertProductPage(userOrders));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserInfo user = (UserInfo) auth.getPrincipal();

        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemResponse>> findOrderItems(@PathVariable Long orderId){
        List<OrderItemResponse> response = orderService.findOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId, @RequestParam String newStatus){
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e){
            throw new BadRequestException("Cannot Update status with status "+ newStatus);
        }
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/total")
    public ResponseEntity<Double> calculateOrderTotal(@PathVariable Long orderId){
        double total = orderService.calculateOrderTotal(orderId);
        return ResponseEntity.ok(total);
    }
}
