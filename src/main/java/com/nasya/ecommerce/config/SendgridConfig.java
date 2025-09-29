package com.nasya.ecommerce.config;

import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class SendgridConfig {

    @Value("${sendgrid.ap-key}")
    private String SENDGRID_API_KEY;
    @Value("${sendgrid.from-email}")
    private String SENDGRID_FROM_EMAIL;
    @Value("${sendgrid.email-template.payment-success}")
    private String SENDGRID_TEMPLATE_PAYMENT_SUCCESS;
    @Value("${sendgrid.email-template.payment-failed}")
    private String SENDGRID_TEMPLATE_PAYMENT_FAILED;

    @Bean
   public SendGrid sendGrid(){
       return new SendGrid(SENDGRID_API_KEY);
   }

   @Bean
   public Email fromEmail(){
        return new Email(SENDGRID_FROM_EMAIL, "nasyaShop");
   }

//   public String getTemplatePaymentSuccess(){
//        return SENDGRID_TEMPLATE_PAYMENT_SUCCESS;
//   }
//
//   public String getTemplatePaymentFailed(){
//        return SENDGRID_TEMPLATE_PAYMENT_FAILED;
//   }
}
