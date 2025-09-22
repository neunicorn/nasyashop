package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.*;
import com.nasya.ecommerce.entity.Role;
import com.nasya.ecommerce.entity.User;
import com.nasya.ecommerce.entity.UserRole;
import com.nasya.ecommerce.entity.UserRole.UserRoleId;
import com.nasya.ecommerce.model.request.user.UserRegisterRequest;
import com.nasya.ecommerce.model.request.user.UserUpdateRequest;
import com.nasya.ecommerce.model.response.user.UserResponse;
import com.nasya.ecommerce.repository.RoleRepository;
import com.nasya.ecommerce.repository.UserRepository;
import com.nasya.ecommerce.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    private final CacheService cacheService;
    private final String USER_CACHE_KEY = "cache:user:";
    private final String USER_ROLES_CACHE_KEY = "cache:user:role";

    @Override
    @Transactional
    public UserResponse register(UserRegisterRequest request) {

        //check username already used or no
        if(existByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already taken");
        }
        // check email exist
        if(existByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail()+ " already exists");
        }
        //check if password was same if not throw error
        if(!request.getPassword().equals(request.getPasswordConfirmation())){
            throw new BadRequestException("password was wrong!");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .enabled(true)
                .build();
        userRepository.save(user);

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(()-> new RoleNotFoundException("Role not found"));

        UserRole userRole = UserRole.builder()
                .id(new UserRoleId(user.getUserId(), role.getRoleId()))
                .build();
        userRoleRepository.save(userRole);

        return UserResponse.fromUserAndRoles(user, List.of(role));
    }

    @Override
    public UserResponse findById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("user not found with id: " + userId));

        List<Role> roles = roleRepository.findByUserId(userId);

        return UserResponse.fromUserAndRoles(user, roles);
    }

    @Override
    public UserResponse findByKeyword(String keyword) {
        User user = userRepository.findByKeyword(keyword)
                .orElseThrow(()-> new UserNotFoundException("user not found with keyword: " + keyword));

        List<Role> roles = roleRepository.findByUserId(user.getUserId());
        return UserResponse.fromUserAndRoles(user, roles);

    }

    @Override
    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("user not found with id: " + userId));

        //update password user if not null
        if(request.getCurrentPassword() != null && request.getNewPassword() != null){
            if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())){
                throw new InvalidPasswordException("current password is incorrect");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // update username if username not null
        if(request.getUsername() != null && !request.getUsername().equals(user.getUsername())){
            if(existByUsername(request.getUsername())){
                throw new UsernameAlreadyExistsException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }

        // update email if email not null
        if(request.getEmail() != null && !request.getEmail().equals(user.getEmail())){
            if(existByEmail(request.getEmail())){
                throw new EmailAlreadyExistsException("Email already taken");
            }
            user.setEmail(request.getEmail());
        }
        List<Role> roles = roleRepository.findByUserId(userId);
        userRepository.save(user);

        // delete cache because the data has been changed
        cacheService.evict(USER_CACHE_KEY + user.getUsername());
        cacheService.evict(USER_ROLES_CACHE_KEY + roles);

        return UserResponse.fromUserAndRoles(user, roles);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("user not found with id: " + userId));

        List<UserRole> userRole = userRoleRepository.findByUserId(userId);

        userRoleRepository.deleteAll(userRole);
        userRepository.delete(user);
    }

    @Override
    public boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
