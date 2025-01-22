package com.nasya.ecommerce.model.request.checkout;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CheckoutRequest {

    private Long userId;

    @NotEmpty(message = "At least one cart items must be selected")
    @Size(min = 1)
    private List<Long> selectedCartItemIds;

    @NotNull(message = "user Address Id is required")
    private Long userAddressId;
}
