package com.nasya.ecommerce.service;

import com.nasya.ecommerce.model.request.auth.AuthRequest;
import com.nasya.ecommerce.security.UserInfo;
import org.springframework.stereotype.Service;

public interface AuthService {
    UserInfo authenticate(AuthRequest authRequest);
}
