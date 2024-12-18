package com.nasya.ecommerce.security;

import org.springframework.stereotype.Component;

public interface JwtProvider {

    String generateToken(UserInfo userInfo);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
}
