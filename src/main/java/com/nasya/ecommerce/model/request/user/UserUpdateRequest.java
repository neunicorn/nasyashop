package com.nasya.ecommerce.model.request.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class UserUpdateRequest {

    @Size(min = 3, max=50, message="username must be between 3 and 50 char")
    private String username;

    @Email(message = "invalid email format")
    private String email;

    @Size(min = 8, max=100, message="password must be between 3 and 50 char")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain minimum eight characters, at least one uppercase letter, " +
                    "one lowercase letter, one number and one special character")
    private String newPassword;

    private String currentPassword;


}
