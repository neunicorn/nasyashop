package com.nasya.ecommerce.service;

import com.nasya.ecommerce.entity.Cart;
import com.nasya.ecommerce.model.response.cart.CartItemResponse;

import java.util.List;

public interface CartService {

    void addItemToCart(Long userId, Long productId, Integer quantity);
    void updateCartItemQuantity(Long userId, Long productId, Integer quantity);
    void removeItemFromCart(Long userId, Long cartItemId);

    void clearCart(Long userId);

    List<CartItemResponse> getCartItems(Long userId);
}
