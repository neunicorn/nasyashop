package com.nasya.ecommerce.controller;

import com.nasya.ecommerce.model.request.auth.AuthRequest;
import com.nasya.ecommerce.model.request.user.UserRegisterRequest;
import com.nasya.ecommerce.model.response.auth.AuthResponse;
import com.nasya.ecommerce.model.response.user.UserResponse;
import com.nasya.ecommerce.security.JwtProvider;
import com.nasya.ecommerce.security.UserInfo;
import com.nasya.ecommerce.service.AuthService;
import com.nasya.ecommerce.service.AuthServiceImpl;
import com.nasya.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request){
        UserInfo user = authService.authenticate(request);
        String token = jwtProvider.getUsernameFromToken(user.getUsername());

        return ResponseEntity.ok(AuthResponse.fromUserInfo(user, token));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request){

        UserResponse res = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }


}
