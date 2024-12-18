package com.nasya.ecommerce.controller;

import com.nasya.ecommerce.model.request.user.UserUpdateRequest;
import com.nasya.ecommerce.model.response.user.UserResponse;
import com.nasya.ecommerce.security.UserInfo;
import com.nasya.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
@SecurityRequirement(name = "Bearer")
public class UserController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();

        UserInfo userInfo = (UserInfo) userDetailsService.loadUserByUsername(name);

        UserResponse res = UserResponse.fromUserAndRoles(userInfo.getUser(), userInfo.getRoles());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateById(@PathVariable Long id,
                                                  @Valid @RequestBody UserUpdateRequest request){
        return null;
    }
}
