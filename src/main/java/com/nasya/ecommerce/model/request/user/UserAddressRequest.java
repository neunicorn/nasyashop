package com.nasya.ecommerce.model.request.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserAddressRequest {

    @NotBlank
    @Size(min = 100)
    private String addressName;

    @NotBlank
    @Size(min = 255)
    private String addressStreet;

    @NotBlank
    @Size(min = 100)
    private String city;

    @NotBlank
    @Size(min = 100)
    private String state;

    @NotBlank
    @Size(min = 25)
    private String postalCode;

    @NotBlank
    @Size(min = 100)
    private String country;

    private boolean isDefault;

}
