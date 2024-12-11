package com.nasya.ecommerce.service;

import com.nasya.ecommerce.model.request.user.UserRegisterRequest;
import com.nasya.ecommerce.model.request.user.UserUpdateRequest;
import com.nasya.ecommerce.model.response.user.UserResponse;

public interface UserService {

    UserResponse register(UserRegisterRequest request);

    UserResponse findById(Long userId);

    /**
     * Service for find user by it username or email
     * @param keyword can be username or email
     * @return UserResponse
     */
    UserResponse findByKeyword(String keyword);

    UserResponse update (Long userId, UserUpdateRequest request);

    void deleteUser(Long userId);

    boolean existByUsername(String username);
    boolean existByEmail(String email);

}
