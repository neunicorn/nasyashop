package com.nasya.ecommerce.model.request.checkout;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasya.ecommerce.entity.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShippingRateRequest {

    private Address fromAddress;
    private Address toAddress;
    private BigDecimal totalWeightInGrams;


    @Data
    @Builder
    public static class Address {
        private String streetAddress;
        private String city;
        private String state;
        private String postalCode;
    }

    public static Address fromUserAddress(UserAddress userAddress){
        return Address.builder().build();
    }
}
