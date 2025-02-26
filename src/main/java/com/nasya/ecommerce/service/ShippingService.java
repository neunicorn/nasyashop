package com.nasya.ecommerce.service;

import com.nasya.ecommerce.model.request.checkout.ShippingOrderRequest;
import com.nasya.ecommerce.model.request.checkout.ShippingRateRequest;
import com.nasya.ecommerce.model.response.order.ShippingOrderResponse;
import com.nasya.ecommerce.model.response.order.ShippingRateResponse;

import java.math.BigDecimal;

public interface ShippingService {

    ShippingRateResponse calculateShippingRate(ShippingRateRequest request);
    ShippingOrderResponse createShippingOrder(ShippingOrderRequest request);
    String generateAwbNumber(Long orderId);
    BigDecimal calculateTotalWeight(Long orderId);
}
