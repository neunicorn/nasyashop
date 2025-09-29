package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.UserNotFoundException;
import com.nasya.ecommerce.config.SendgridConfig;
import com.nasya.ecommerce.entity.Order;
import com.nasya.ecommerce.entity.User;
import com.nasya.ecommerce.model.OrderStatus;
import com.nasya.ecommerce.repository.UserRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final SendgridConfig sendgridConfig;
    private final UserRepository userRepository;
    private final SendGrid sendGrid;
    private final Retry emailRetier;

    @Override
    @Async
    public void notifySuccessfulPayment(Order order) {
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Mail mail = sendEmailPaymentSuccess(user, order);
        sendEmailWithRetry(mail);

    }

    @Override
    @Async
    public void notifyFailedPayment(Order order) {
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Mail mail = sendEmailPaymentFailed(user, order);
        sendEmailWithRetry(mail);
    }

    private void sendEmail(Mail mail) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response res = sendGrid.api(request);
        if(res.getStatusCode() > 299){
            log.error("Error while sending email. Status code: " + res.getStatusCode());
            throw new IOException("Error while sending email. Status code: " + res.getStatusCode());
        }
    }

    private Mail sendEmailPaymentSuccess(User user, Order order){

        Email from = new Email(sendgridConfig.getSENDGRID_FROM_EMAIL());
        Email to = new Email(user.getEmail());
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setReplyTo(from);
        mail.setTemplateId(sendgridConfig.getSENDGRID_TEMPLATE_PAYMENT_SUCCESS());

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("customerName", user.getUsername());
        personalization.addDynamicTemplateData("price", order.getTotalAmount().toString());
        personalization.addDynamicTemplateData("orderId", order.getOrderId());
        personalization.addDynamicTemplateData("datePayment",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        mail.addPersonalization(personalization);

        return mail;
    };

    private Mail sendEmailPaymentFailed(User user, Order order){

        Email from = new Email(sendgridConfig.getSENDGRID_FROM_EMAIL());
        Email to = new Email(user.getEmail());
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setReplyTo(from);
        mail.setTemplateId(sendgridConfig.getSENDGRID_TEMPLATE_PAYMENT_FAILED());

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("customerName", user.getUsername());
        personalization.addDynamicTemplateData("price", order.getTotalAmount().toString());
        personalization.addDynamicTemplateData("orderId", order.getOrderId());
        personalization.addDynamicTemplateData("datePayment",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        personalization.addDynamicTemplateData("reasonFailed", failedReasonMsg(order.getStatus()));

        mail.addPersonalization(personalization);

        return mail;
    };

    private String failedReasonMsg(OrderStatus status){
        return switch(status){
            case CANCELLED -> "Pembayaran telah kadaluarsa. Silakan melakukan pemesanan ulang";
            case PAYMENT_FAILED -> "Pembayaran gagal di proses. Mohon periksa metode pembayaran Anda dan coba lagi";
            case PENDING -> "Pembayaran masih dalam proses. Mohon tunggu beberapa saat lagi";
            default -> "Terjadi kesalahan dalam proses pembayaran. Silakan hubungi layanan pelanggan kami";
        };
    }

    private void sendEmailWithRetry(Mail mail){
        try{
        emailRetier.executeCallable(()->{
            sendEmail(mail);
            return null;
        });
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
