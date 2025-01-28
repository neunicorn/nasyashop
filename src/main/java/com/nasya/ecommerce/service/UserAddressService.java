package com.nasya.ecommerce.service;

import com.nasya.ecommerce.entity.UserAddress;
import com.nasya.ecommerce.model.request.user.UserAddressRequest;
import com.nasya.ecommerce.model.response.user.UserAddressResponse;

import java.util.List;

public interface UserAddressService {

    UserAddressResponse create(Long userId, UserAddressRequest request);

    List<UserAddressResponse> findByUserId(Long userId);

    UserAddressResponse findById(Long id);

    UserAddressResponse update(Long addressId, UserAddressRequest request);

    void delete(Long id);

    UserAddressResponse setDefaultAddress(Long userId, Long addressId);
}
