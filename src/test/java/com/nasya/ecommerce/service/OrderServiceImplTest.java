package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.erros.ResourceNotFoundException;
import com.nasya.ecommerce.entity.*;
import com.nasya.ecommerce.model.OrderStatus;
import com.nasya.ecommerce.model.request.checkout.CheckoutRequest;
import com.nasya.ecommerce.model.response.order.OrderResponse;
import com.nasya.ecommerce.model.response.order.PaymentResponse;
import com.nasya.ecommerce.model.response.order.ShippingRateResponse;
import com.nasya.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserAddressRepository userAddressRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ShippingService shippingService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    private CheckoutRequest checkoutRequest;
    private List<CartItem> cartItems;
    private UserAddress userAddress;
    private Product product;
    private UserAddress sellerAddress;
    private User seller;
    private User buyer;


    @BeforeEach
    void setUp(){
        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setUserId(1L);
        checkoutRequest.setUserAddressId(1L);
        checkoutRequest.setSelectedCartItemIds(List.of(1L, 2L));

        cartItems = new ArrayList<>();
        CartItem cartItem1 = new CartItem();
        cartItem1.setProductId(1L);
        cartItem1.setCartItemId(1L);
        cartItem1.setQuantity(2);
        cartItem1.setPrice(new BigDecimal(10000));
        cartItems.add(cartItem1);

        CartItem cartItem2 = new CartItem();
        cartItem2.setProductId(2L);
        cartItem2.setCartItemId(2L);
        cartItem2.setQuantity(1);
        cartItem2.setPrice(new BigDecimal(20000));
        cartItems.add(cartItem2);

        userAddress = new UserAddress();
        userAddress.setUserAddressId(1L);
//        userAddress.setUserId(buyer.getUserId());

        seller = new User();
        seller.setUserId(1L);
        buyer = new User();
        buyer.setUserId(2L);


        sellerAddress = new UserAddress();
        sellerAddress.setUserAddressId(2L);
        sellerAddress.setUserId(seller.getUserId());

        product = new Product();
        product.setProductId(1L);
        product.setWeight(new BigDecimal("0.5"));
        product.setUserId(seller.getUserId());

    }

    @Test
    void testCheckout_Successfull_Checkout() {
        //arrange
        when(cartItemRepository.findAllById(anyList())).thenReturn(cartItems);
        when(userAddressRepository.findById(anyLong())).thenReturn(Optional.of(userAddress));
        when(inventoryService.checkAndLockInventory(anyMap())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(userAddressRepository.findByUserIdAndIsDefaultTrue(anyLong())).thenReturn(
                Optional.of(sellerAddress));

        ShippingRateResponse shippingRateResponse = new ShippingRateResponse();
        shippingRateResponse.setShippingFee(new BigDecimal("10.00"));
        when(shippingService.calculateShippingRate(any())).thenReturn(shippingRateResponse);

        PaymentResponse paymentResponse=  new PaymentResponse();
        paymentResponse.setXenditInvoiceId("payment123");
        paymentResponse.setXenditInvoiceStatus("PENDING");
        paymentResponse.setXenditPaymentUrl("http://payment.url");
        when(paymentService.create(any())).thenReturn(paymentResponse);

        //Act
        OrderResponse result = orderServiceImpl.checkout(checkoutRequest);

        //Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals("payment123", result.getXenditInvoiceId());
        assertEquals("http://payment.url", result.getXenditPaymentUrl());

        verify(cartItemRepository).findAllById(checkoutRequest.getSelectedCartItemIds());
        verify(userAddressRepository).findById(checkoutRequest.getUserAddressId());
        verify(inventoryService).checkAndLockInventory(anyMap());
        verify(orderRepository, times(3)).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
        verify(cartItemRepository).deleteAll(cartItems);
        verify(shippingService, times(2)).calculateShippingRate(any());
        verify(paymentService).create(any());
        verify(inventoryService).decreaseQuantity(anyMap());
        verify(userAddressRepository, times(2)).findByUserIdAndIsDefaultTrue(anyLong());

    }

    @Test
    void testCheckout_WhenCartIsEmpty(){
        when(cartItemRepository.findAllById(anyList())).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, ()-> orderServiceImpl.checkout(checkoutRequest));
    }

}
