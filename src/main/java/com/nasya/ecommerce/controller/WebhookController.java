package com.nasya.ecommerce.controller;

import com.nasya.ecommerce.model.response.order.PaymentNotification;
import com.nasya.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;

    @PostMapping("/xendit")
    public ResponseEntity<String> handleXenditWebhook(@RequestBody PaymentNotification paymentNotification){

        paymentService.handleNotification(paymentNotification);
        return ResponseEntity.ok("OK");
    }

}
