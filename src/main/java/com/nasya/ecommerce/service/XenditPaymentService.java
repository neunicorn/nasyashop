package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.ResourceNotFoundException;
import com.nasya.ecommerce.entity.Order;
import com.nasya.ecommerce.entity.User;
import com.nasya.ecommerce.model.OrderStatus;
import com.nasya.ecommerce.model.response.order.OrderResponse;
import com.nasya.ecommerce.model.response.order.PaymentNotification;
import com.nasya.ecommerce.model.response.order.PaymentResponse;
import com.nasya.ecommerce.repository.OrderRepository;
import com.nasya.ecommerce.repository.UserRepository;
import com.xendit.exception.XenditException;
import com.xendit.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class XenditPaymentService implements PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;


    @Override
    public PaymentResponse create(Order order) {

        User user = userRepository.findById(order.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("User Not For That Order Not Found"));

        Map<String, Object> params = new HashMap<>();
        params.put("external_id", order.getOrderId().toString());
        params.put("amount", order.getTotalAmount().doubleValue());
        params.put("payer_email", user.getEmail());
        params.put("description", "Payment for Order #"+order.getOrderId().toString());

        try {
        Invoice invoice = Invoice.create(params);
            return PaymentResponse.builder()
                    .xenditPaymentUrl(invoice.getInvoiceUrl())
                    .xenditExternalId(invoice.getExternalId())
                    .xenditInvoiceId(invoice.getId())
                    .amount(order.getTotalAmount())
                    .xenditInvoiceStatus(invoice.getStatus())
                    .build();
        } catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaymentResponse findByPaymentId(String paymentId) {
        try {
            Invoice invoice = Invoice.getById(paymentId);
            return PaymentResponse.builder()
                    .xenditPaymentUrl(invoice.getInvoiceUrl())
                    .xenditExternalId(invoice.getExternalId())
                    .xenditInvoiceId(invoice.getId())
                    .amount(BigDecimal.valueOf(invoice.getAmount().doubleValue()))
                    .xenditInvoiceStatus(invoice.getStatus())
                    .build();
        }catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  This Function is for check is the payment already PAID or not
     * @param paymentId
     * @return boolean
     */
    @Override
    public boolean verifyByPaymentId(String paymentId) {
        try {
            Invoice invoice = Invoice.getById(paymentId);
            return "PAID".equals(invoice.getStatus());
        } catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleNotification(PaymentNotification paymentNotification) {

        String invoiceId = paymentNotification.getId();
        String status = paymentNotification.getStatus();

        Order order = orderRepository.findByXenditInvoiceId(invoiceId)
                .orElseThrow(()-> new ResourceNotFoundException("Order Not Found with invoice id "+ invoiceId));

        order.setXenditPaymentStatus(status);
        switch (status) {
            case "PAID":
                order.setStatus(OrderStatus.PAID);
                break;
            case "EXPIRED":
                order.setStatus(OrderStatus.CANCELLED);
                break;
            case  "FAILED":
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                break;
            case "PENDING":
                order.setStatus(OrderStatus.PENDING);
                break;
            default:
        }

        if(paymentNotification.getPaymentMethod() != null){
            order.setXenditPaymentMethod(paymentNotification.getPaymentMethod());
        }

        orderRepository.save(order);
    }

    @Override
    public void cancelXenditInvoice(Order order) {
        try{
            Invoice invoice = Invoice.expire(order.getXenditInvoiceId());
            order.setXenditPaymentStatus(invoice.getStatus());
            orderRepository.save(order);
        } catch (XenditException e) {
            log.error("Error while request invoice cancellation with xendit invoice id: " + order.getXenditInvoiceId());
            throw new RuntimeException(e);
        }
    }
}
