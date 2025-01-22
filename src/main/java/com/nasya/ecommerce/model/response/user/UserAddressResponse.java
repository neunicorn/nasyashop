package com.nasya.ecommerce.model.response.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasya.ecommerce.entity.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserAddressResponse implements Serializable {

    private Long userAddressId;
    private String addressName;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public static UserAddressResponse fromUserAddress(UserAddress userAddress){
        return UserAddressResponse.builder()
                .userAddressId(userAddress.getUserAddressId())
                .addressName(userAddress.getAddressName())
                .streetAddress(userAddress.getStreetAddress())
                .city(userAddress.getCity())
                .state(userAddress.getState())
                .postalCode(userAddress.getPostalCode())
                .country(userAddress.getCountry())
                .build();
    }
}
