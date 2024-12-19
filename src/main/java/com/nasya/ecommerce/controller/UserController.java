package com.nasya.ecommerce.controller;

import com.nasya.ecommerce.common.erros.ForbiddenAccessException;
import com.nasya.ecommerce.model.request.user.UserUpdateRequest;
import com.nasya.ecommerce.model.response.user.UserResponse;
import com.nasya.ecommerce.security.CustomUserDetails;
import com.nasya.ecommerce.security.UserInfo;
import com.nasya.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
@SecurityRequirement(name = "Bearer")
@Slf4j
public class UserController {

    private final UserService userService;
    private final CustomUserDetails userDetailsService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();

        UserInfo userInfo = (UserInfo) userDetailsService.loadUserByUsername(name);

        UserResponse res = UserResponse.fromUserAndRoles(userInfo.getUser(), userInfo.getRoles());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                  @Valid @RequestBody UserUpdateRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo user = (UserInfo) auth.getPrincipal();

        if(!Objects.equals(user.getUser().getUserId(), id) && !user.getAuthorities().contains("ROLE_ADMIN")){
            throw new ForbiddenAccessException("user " + user.getUsername() + " not allowed update");
        }

        UserResponse res = userService.update(id, request);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo user = (UserInfo) auth.getPrincipal();

        if(!Objects.equals(user.getUser().getUserId(), id) && !user.getAuthorities().contains("ROLE_ADMIN")){
            throw new ForbiddenAccessException("user " + user.getUsername() + " not allowed update");
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyword}")
    public ResponseEntity<UserResponse> findUserByKeyword(@PathVariable String keyword){
        UserResponse res = userService.findByKeyword(keyword);

        return ResponseEntity.ok(res);
    }
}
