package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.BadRequestException;
import com.nasya.ecommerce.common.erros.InventoryException;
import com.nasya.ecommerce.common.erros.ResourceNotFoundException;
import com.nasya.ecommerce.entity.Cart;
import com.nasya.ecommerce.entity.CartItem;
import com.nasya.ecommerce.entity.Product;
import com.nasya.ecommerce.model.response.cart.CartItemResponse;
import com.nasya.ecommerce.repository.CartItemRepository;
import com.nasya.ecommerce.repository.CartRepository;
import com.nasya.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void addItemToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(()->{
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .build();
            return cartRepository.save(newCart);
        });

        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product with id " + productId + " is not found" ));
        if(product.getUserId().equals(userId)) {
            throw new BadRequestException("Cannot add your own product to cart");
        }

        if(product.getStockQuantity() <= 0){
            throw new InventoryException("Product with id " + productId + " is not enough stock");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getCartId(), product.getProductId());
        if(existingItem.isPresent()) {
            CartItem existingItemTemp = existingItem.get();
            existingItemTemp.setQuantity(existingItemTemp.getQuantity() + quantity);
            cartItemRepository.save(existingItemTemp);
        } else {
            CartItem newCartItem = CartItem.builder()
                    .cartId(cart.getCartId())
                    .productId(product.getProductId())
                    .quantity(quantity)
                    .price(product.getPrice())

                    .build();
            cartItemRepository.save(newCartItem);
        }


    }

    @Override
    public void updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("cart with userid " + userId + " is not found" ));

        Optional<CartItem> cartItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getCartId(), productId);
        if(cartItemOpt.isEmpty()){
            throw new ResourceNotFoundException("Cart Item not found!");
        }

        CartItem cartItem = cartItemOpt.get();
        if(quantity <= 0){
            cartItemRepository.deleteById(cartItem.getCartItemId());
        } else{
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Override
    public void removeItemFromCart(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("cart with userid " + userId + " is not found" ));

        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if(cartItemOpt.isEmpty()){
            throw new ResourceNotFoundException("Cart Item not found!");
        }

        CartItem item = cartItemOpt.get();
        if(!item.getCartId().equals(cart.getCartId())){
            throw new BadRequestException("Cart Item is not in your cart");
        }
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("cart with userid " + userId + " is not found" ));
        cartItemRepository.deleteAllByCartId(cart.getCartId());
    }

    @Override
    public List<CartItemResponse> getCartItems(Long userId) {

        List<CartItem> cartItems = cartItemRepository.getUserCartItems(userId);
        if(cartItems.isEmpty()){
            return Collections.emptyList();
        }

        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .toList();

        List<Product> products = productRepository.findAllById(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        return cartItems.stream()
                .map(cartItem-> {
                    Product product = productMap.get(cartItem.getProductId());
//                    Product product2 = products.stream().filter(item -> product.getProductId().equals(cartItem.getProductId())).findFirst().get();
                    return CartItemResponse.fromCartItemAndProduct(cartItem, product);
                }).toList();
    }
}
