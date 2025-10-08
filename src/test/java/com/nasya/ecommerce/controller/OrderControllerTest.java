//package com.nasya.ecommerce.controller;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nasya.ecommerce.entity.Role;
//import com.nasya.ecommerce.entity.User;
//import com.nasya.ecommerce.model.OrderStatus;
//import com.nasya.ecommerce.model.request.checkout.CheckoutRequest;
//import com.nasya.ecommerce.model.response.order.OrderResponse;
//import com.nasya.ecommerce.security.UserInfo;
//import com.nasya.ecommerce.service.OrderService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc(addFilters = false)
//@DisplayName("Order Controller")
//class OrderControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private OrderService orderService;
//
//    private CheckoutRequest req;
//    private OrderResponse res;
//    private UserInfo userInfo;
//
//
//    @BeforeEach
//    void setUp(){
//        req = CheckoutRequest.builder()
//                .selectedCartItemIds(List.of(1L, 2L))
//                .userAddressId(1L)
//                .build();
//
//        res = OrderResponse.builder()
//                .orderId(1L)
//                .status(OrderStatus.PENDING)
//                .totalAmount(new BigDecimal("100.00"))
//                .shippingFee(new BigDecimal("10.00"))
//                .xenditPaymentUrl("http://payment.uri")
//                .build();
//
//        User user = User.builder()
//                .userId(1L)
//                .username("user@example.com")
//                .email("user@example.com")
//                .password("Password12!")
//                .enabled(true)
//                .build();
//
//        Role role = new Role();
//        role.setName("ROLE_USER");
//
//        userInfo = UserInfo.builder()
//                .user(user)
//                .roles(List.of(role))
//                .build();
//
//        SecurityContext securityContext = mock(SecurityContext.class);
//        SecurityContextHolder.setContext(securityContext);
//
//        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
//                userInfo, null, userInfo.getAuthorities()
//        );
//        when(securityContext.getAuthentication()).thenReturn(auth);
//    }
//
//    @Nested
//    @DisplayName("When checkout controller hit")
//    class CheckoutTest {
//
//        @Test
//        @DisplayName("it should make a checkout and  return 201")
//        void checkout_when_request_valid() throws Exception {
//            when(orderService.checkout(req)).thenReturn(res);
//
//            mockMvc.perform(post("/orders/checkout")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(req)))
//                    .andExpectAll(status().isOk())
//                    .andDo(res -> {
//                        ResponseEntity<OrderResponse> response = objectMapper.readValue(
//                                res.getResponse().getContentAsString(),
//                                new TypeReference<ResponseEntity<OrderResponse>>() {
//                                });
//
//                        assertNotNull(response);
//                        assertEquals("PENDING", response.getBody().getStatus());
//                    });
////                    .andExpect(jsonPath("$.order_id").value(1))
////                    .andExpect(jsonPath("$.total_amount").value("100.00"))
////                    .andExpect(jsonPath("$.shipping_fee").value("10.00"))
////                    .andExpect(jsonPath("$.payment_url").value("http://payment.uri"))
////                    .andExpect(jsonPath("$.status").value("PENDING"));
//
//            verify(orderService).checkout(argThat(request->
//                request.getUserId().equals(1L)
//                        && request.getSelectedCartItemIds().equals(req.getSelectedCartItemIds())
//                        && request.getUserAddressId().equals(req.getUserAddressId())));
//
//        }
//    }
//}