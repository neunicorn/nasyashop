package com.nasya.ecommerce.service;

import com.nasya.ecommerce.entity.Order;
import com.nasya.ecommerce.model.response.order.PaymentNotification;
import com.nasya.ecommerce.model.response.order.PaymentResponse;

public interface PaymentService {

    PaymentResponse create(Order order);

    PaymentResponse findByPaymentId(String paymentId);

    boolean verifyByPaymentId(String paymentId);

    void handleNotification(PaymentNotification paymentNotification);

    void cancelXenditInvoice(Order order);
}
