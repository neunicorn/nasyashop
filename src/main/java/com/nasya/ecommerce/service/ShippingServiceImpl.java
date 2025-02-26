package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.ResourceNotFoundException;
import com.nasya.ecommerce.entity.Order;
import com.nasya.ecommerce.entity.OrderItem;
import com.nasya.ecommerce.entity.Product;
import com.nasya.ecommerce.model.request.checkout.ShippingOrderRequest;
import com.nasya.ecommerce.model.request.checkout.ShippingRateRequest;
import com.nasya.ecommerce.model.response.order.ShippingOrderResponse;
import com.nasya.ecommerce.model.response.order.ShippingRateResponse;
import com.nasya.ecommerce.repository.OrderItemRepository;
import com.nasya.ecommerce.repository.OrderRepository;
import com.nasya.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService{

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(10000);
    private static final BigDecimal RATE_PER_KG = BigDecimal.valueOf(2500);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    public ShippingRateResponse calculateShippingRate(ShippingRateRequest request) {
        // shipping fee = base rate + (weight * rate/kg)
        BigDecimal shippingFee =
                BASE_RATE.add(
                        request.getTotalWeightInGrams().divide(BigDecimal.valueOf(1000))
                                .multiply(RATE_PER_KG))
                        .setScale(2, BigDecimal.ROUND_HALF_UP);

        String estimatedDeliveryTime = "3 - 5 hari kerja";

        return ShippingRateResponse.builder()
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .shippingFee(shippingFee)
                .build();
    }

    @Override
    public ShippingOrderResponse createShippingOrder(ShippingOrderRequest request) {
        String awbNumber = generateAwbNumber(request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(()-> new ResourceNotFoundException("order dengan id "+ request.getOrderId() + " tidak ditemukan"));

        order.setStatus("SHIPPING");
        order.setAwbNumber(awbNumber);
        orderRepository.save(order);

        String estimatedDeliveryTime = "3 - 5 hari kerja";


        return ShippingOrderResponse.builder()
                .awbNumber(awbNumber)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .build();
    }

    @Override
    public String generateAwbNumber(Long orderId) {
        Random random = new Random();
        String prefix = "AWB";
        return String.format("%s%011d", prefix, random.nextInt(100000000));
    }

    @Override
    public BigDecimal calculateTotalWeight(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(()-> new ResourceNotFoundException("item not found"));
                    return product.getWeight().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
