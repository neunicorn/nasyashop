package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.InvalidPasswordException;
import com.nasya.ecommerce.model.request.auth.AuthRequest;
import com.nasya.ecommerce.security.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    @Override
    public UserInfo authenticate(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword())
        );

            return  (UserInfo) authentication.getPrincipal();
        }catch (Exception e){
            throw new InvalidPasswordException("Invalid username or password");
        }
    }
}
