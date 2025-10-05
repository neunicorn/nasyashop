package com.nasya.ecommerce.service;

import com.nasya.ecommerce.entity.Role;
import com.nasya.ecommerce.entity.User;
import com.nasya.ecommerce.entity.UserRole;
import com.nasya.ecommerce.model.request.user.UserRegisterRequest;
import com.nasya.ecommerce.model.request.user.UserUpdateRequest;
import com.nasya.ecommerce.model.response.user.UserResponse;
import com.nasya.ecommerce.repository.RoleRepository;
import com.nasya.ecommerce.repository.UserRepository;
import com.nasya.ecommerce.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterRequest userRegisterRequest;
    private User user;
    private Role role;
    private UserRole userRole;
    private final String USER_CACHE_KEY = "cache:user:";
    private final String USER_ROLES_CACHE_KEY = "cache:user:role";

    @BeforeEach
    void setUp(){
        userRegisterRequest = UserRegisterRequest.builder()
                .username("baehq12")
                .email("baehq12@gmail.com")
                .password("Bukutulis12!!")
                .passwordConfirmation("Bukutulis12!!")
                .build();

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

    }

    @Test
    void test_Register_Should_Return_User() {

        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("HASHED_PASSWORD");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(role));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(userRole);

        //ACT
       UserResponse result =  userService.register(userRegisterRequest);

       // Assert
        assertNotNull(result);
        assertEquals(userRegisterRequest.getUsername(), result.getUsername());
    }

    @Test
    void findById() {

        //Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(roleRepository.findByUserId(anyLong())).thenReturn(List.of(role));

        //Act
        UserResponse res = userService.findById(1L);


        //Assert
        assertNotNull(res);
        assertEquals(user.getUsername(), res.getUsername());
    }

    @Test
    void findByKeyword() {

        // ARrange
        when(userRepository.findByKeyword(anyString())).thenReturn(Optional.of(user));
        when(roleRepository.findByUserId(anyLong())).thenReturn(List.of(role));

        UserResponse res = userService.findByKeyword(user.getUsername());

        assertNotNull(res);

    }

    @Test
    void test_UpdateUserPassword_ShouldReturn_Updated_User(){
        // arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("baehq12")
                .email("baehq12@gmail.com")
                .currentPassword("HASHED_PASSWORD")
                .newPassword("Rahasia12!@")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("NEW_HASHED_PASSWORD");
        when(roleRepository.findByUserId(anyLong())).thenReturn(List.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        //Act
        UserResponse res = userService.update(user.getUserId(), request);

        //assert
        assertNotNull(res);
        assertEquals("NEW_HASHED_PASSWORD", user.getPassword(), "Password must be set to the new encoded hash.");

        // 3. Verify Persistence (The final save was called)
        verify(userRepository, times(1)).save(user);

        // 4. Verify Cache Eviction (Critical I/O call)
        // Ensure the evict was called with the correct *new* username
        verify(cacheService).evict(USER_CACHE_KEY + request.getUsername());

        // Ensure the evict was called for the roles cache
        // Note: The key depends on how List<Role> is converted to a string key in the service.
        // Assuming your 'roles' object is the key, the key should be USER_ROLES_CACHE_KEY + List.of(role).toString()
        // Since List<Role> is complex, let's verify the method was called correctly (using any() for simplicity if the key calculation is complex)
        verify(cacheService).evict(startsWith(USER_ROLES_CACHE_KEY));
    }

    @Test
    void test_DeleteUser_Should_Return_Empty_User(){
        // arrange

        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        when(userRoleRepository.findByUserId(anyLong())).thenReturn(List.of(userRole));

        assertAll(()-> userService.deleteUser(1L));


        verify(userRoleRepository).deleteAll(List.of(userRole)); // Checks if the service called this line
        verify(userRepository).delete(user);

    }
}