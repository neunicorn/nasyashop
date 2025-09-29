package com.nasya.ecommerce.service;

import com.nasya.ecommerce.entity.Order;

public interface EmailService {

    void notifySuccessfulPayment (Order order);

    void notifyFailedPayment (Order order);
}
