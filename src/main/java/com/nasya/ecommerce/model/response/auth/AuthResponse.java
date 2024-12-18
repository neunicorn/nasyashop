package com.nasya.ecommerce.model.response.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasya.ecommerce.entity.Role;
import com.nasya.ecommerce.security.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(SnakeCaseStrategy.class)
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private List<String> roles;

    public static AuthResponse fromUserInfo(UserInfo user, String token){
        return AuthResponse.builder()
                .token(token)
                .userId(user.getUser().getUserId())
                .username(user.getUser().getUsername())
                .email(user.getUser().getEmail())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }
}
