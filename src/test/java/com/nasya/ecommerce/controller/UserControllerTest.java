package com.nasya.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasya.ecommerce.entity.Role;
import com.nasya.ecommerce.entity.User;
import com.nasya.ecommerce.entity.UserRole;
import com.nasya.ecommerce.model.request.user.UserRegisterRequest;
import com.nasya.ecommerce.model.response.user.UserResponse;
import com.nasya.ecommerce.repository.RoleRepository;
import com.nasya.ecommerce.repository.UserRepository;
import com.nasya.ecommerce.repository.UserRoleRepository;
import com.nasya.ecommerce.security.UserInfo;
import com.nasya.ecommerce.service.CacheService;
import com.nasya.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(controllers = UserController.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean //mock the service layer
    private UserService userService;

    private UserRegisterRequest userRegisterRequest;
    private UserResponse userResponse;
    private UserInfo mockUserInfo;
    private Authentication mockAuthentication;

    private User user;
    private Role role;
    private UserRole userRole;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .userId(1L)
                .username("baehq12")
                .email("baehq12@gmail.com")
                .password("HASHED_PASSWORD")
                .enabled(true)
                .build();

        role = Role.builder()
                .name("USER_ROLE")
                .roleId(1L)
                .build();

        userRole = UserRole.builder()
                .id(new UserRole.UserRoleId(user.getUserId(), role.getRoleId()))
                .build();

        mockUserInfo = new UserInfo(user, List.of(role));

        userResponse = UserResponse.fromUserAndRoles(user, List.of(role));

        // --- SIMULATING SECURITY CONTEXT (The Realistic Way) ---

        // 1. Create a real Authentication Token using the UserInfo as the principal.
        // This makes auth.getPrincipal() correctly return mockUserInfo.
        mockAuthentication = new UsernamePasswordAuthenticationToken(
                mockUserInfo,
                null, // Credentials are null after successful authentication
                mockUserInfo.getAuthorities() // Assumes UserInfo provides Authorities
        );
    }
    @Test

    void test_GetUser_Should_Return_User() throws Exception {
        // arrange already do in the setup

        mockMvc.perform(get("/users/me")
                .with(authentication(mockAuthentication))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user_id").value(user.getUserId()));
    }

    @Test
    void test_GetUser_Should_Return_Forbidden() throws Exception {
        // arrange already do in the setup

        mockMvc.perform(get("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void findUserByKeyword() {
    }
}