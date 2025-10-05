package com.nasya.ecommerce.controller;


import com.nasya.ecommerce.model.request.user.UserAddressRequest;
import com.nasya.ecommerce.model.response.user.UserAddressResponse;
import com.nasya.ecommerce.security.UserInfo;
import com.nasya.ecommerce.service.UserAddressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/address")
@SecurityRequirement(name = "Bearer")
@Slf4j
public class AddressController {

    private final UserAddressService userAddressService;

    @PostMapping
    public ResponseEntity<UserAddressResponse> create(@Valid @RequestBody UserAddressRequest request){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo user = (UserInfo) auth.getPrincipal();

        UserAddressResponse res = userAddressService.create(user.getUser().getUserId(), request);

        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> findAddressByUserId(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo user = (UserInfo) auth.getPrincipal();
        log.info(String.valueOf(user));

        List<UserAddressResponse> response = userAddressService.findByUserId(user.getUser().getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> findAddressByAddressId(@PathVariable Long addressId){

        UserAddressResponse response = userAddressService.findById(addressId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> update(@PathVariable Long addressId,
                                                      @Valid @RequestBody UserAddressRequest request){
        UserAddressResponse update = userAddressService.update(addressId, request);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable Long addressId){
        userAddressService.delete(addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<UserAddressResponse> setDefaultAddress(@PathVariable Long addressId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo user = (UserInfo) auth.getPrincipal();

        UserAddressResponse response = userAddressService.setDefaultAddress(user.getUser().getUserId(), addressId);

        return ResponseEntity.ok(response);
    }

}
