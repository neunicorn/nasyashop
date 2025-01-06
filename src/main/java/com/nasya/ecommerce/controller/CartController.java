package com.nasya.ecommerce.controller;

import com.nasya.ecommerce.model.request.cart.AddToCartRequest;
import com.nasya.ecommerce.model.request.cart.UpdateCartItemRequest;
import com.nasya.ecommerce.model.response.cart.CartItemResponse;
import com.nasya.ecommerce.security.UserInfo;
import com.nasya.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/carts")
@SecurityRequirement(name = "Bearer")
@Slf4j
public class CartController {

    private final CartService   cartService;

    @PostMapping("/items")
    public ResponseEntity<Void> addItemToCart(@Valid @RequestBody AddToCartRequest req){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) auth.getPrincipal();

        cartService.addItemToCart(userInfo.getUser().getUserId(), req.getProductId(), req.getQuantity());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items")
    public ResponseEntity<Void> updateItemQuantity(@Valid @RequestBody UpdateCartItemRequest req){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) auth.getPrincipal();

        cartService.updateCartItemQuantity(userInfo.getUser().getUserId(), req.getProductId(), req.getQuantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable("id") Long cartId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) auth.getPrincipal();

        cartService.removeItemFromCart(userInfo.getUser().getUserId(), cartId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> clearCart(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) auth.getPrincipal();

        cartService.clearCart(userInfo.getUser().getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getCartItems(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) auth.getPrincipal();

        List<CartItemResponse> items = cartService.getCartItems(userInfo.getUser().getUserId());

        return ResponseEntity.ok(items);
    }

}
