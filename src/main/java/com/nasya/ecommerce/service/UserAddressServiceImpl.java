package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.ForbiddenAccessException;
import com.nasya.ecommerce.common.erros.ResourceNotFoundException;
import com.nasya.ecommerce.entity.UserAddress;
import com.nasya.ecommerce.model.request.user.UserAddressRequest;
import com.nasya.ecommerce.model.response.user.UserAddressResponse;
import com.nasya.ecommerce.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;

    @Override
    @Transactional
    public UserAddressResponse create(Long userId, UserAddressRequest request) {

        UserAddress newAddress = UserAddress.builder()
                .userId(userId)
                .addressName(request.getAddressName())
                .streetAddress(request.getAddressStreet())
                .state(request.getState())
                .city(request.getCity())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .isDefault(request.isDefault())
                .build();

        //set recent default address to false if the new address default is true
        if(request.isDefault()){
            Optional<UserAddress> existingDefault = userAddressRepository.findByUserIdAndIsDefaultTrue(userId);
            existingDefault.ifPresent(address -> {
                address.setDefault(false);
                userAddressRepository.save(address);
            });
        }

        UserAddress savedAddress = userAddressRepository.save(newAddress);
        return UserAddressResponse.fromUserAddress(savedAddress);
    }

    @Override
    public List<UserAddressResponse> findByUserId(Long userId) {

        List<UserAddress> userAddresses = userAddressRepository.findByUserId(userId);

        if(userAddresses.isEmpty()){
            throw new ResourceNotFoundException("user has not created an address");
        }

        return userAddresses.stream()
                .map(UserAddressResponse::fromUserAddress)
                .toList();
    }

    @Override
    public UserAddressResponse findById(Long id) {

        UserAddress userAddress = userAddressRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("user address with id " + id + " not found"));

        return UserAddressResponse.fromUserAddress(userAddress);
    }

    @Override
    @Transactional
    public UserAddressResponse update(Long addressId, UserAddressRequest request) {
        UserAddress userAddress = userAddressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("user address with id " + addressId + " not found"));


        UserAddress updatedAddress = UserAddress.builder()
                .userAddressId(userAddress.getUserAddressId())
                .addressName(request.getAddressName())
                .streetAddress(request.getAddressStreet())
                .state(request.getState())
                .city(request.getCity())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .isDefault(request.isDefault())
                .build();

        if(request.isDefault() && !userAddress.isDefault()){
            Optional<UserAddress> existingDefault = userAddressRepository
                    .findByUserIdAndIsDefaultTrue(userAddress.getUserId());
            existingDefault.ifPresent(address -> {
                address.setDefault(false);
                userAddressRepository.save(address);
            });
        }
        UserAddress savedAddress = userAddressRepository.save(updatedAddress);
        return UserAddressResponse.fromUserAddress(savedAddress);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        UserAddress userAddress = userAddressRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("user address with id " + id + " not found"));
        userAddressRepository.delete(userAddress);

        if(userAddress.isDefault()){
            List<UserAddress> remainingAddress = userAddressRepository.findByUserId(userAddress.getUserId());
            if(!remainingAddress.isEmpty()){
                UserAddress newDefaultAddress = remainingAddress.getFirst();
                newDefaultAddress.setDefault(true);
                userAddressRepository.save(newDefaultAddress);
            }
        }
    }

    @Override
    public UserAddressResponse setDefaultAddress(Long userId, Long addressId) {
        UserAddress userAddress = userAddressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("user address with id " + addressId + " not found"));

        if(!userAddress.getUserId().equals(userId)){
            throw new ForbiddenAccessException("User Cannot Update the Address");
        }

        Optional<UserAddress> existingDefault = userAddressRepository
                .findByUserIdAndIsDefaultTrue(userId);
        existingDefault.ifPresent(address -> {
            address.setDefault(false);
            userAddressRepository.save(address);
        });
        userAddress.setDefault(true);
        UserAddress save = userAddressRepository.save(userAddress);

        return UserAddressResponse.fromUserAddress(save);
    }
}
