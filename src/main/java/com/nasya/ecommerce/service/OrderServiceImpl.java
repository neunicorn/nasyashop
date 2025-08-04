package com.nasya.ecommerce.service;

import com.nasya.ecommerce.common.OrderStateTransition;
import com.nasya.ecommerce.common.erros.InventoryException;
import com.nasya.ecommerce.common.erros.ResourceNotFoundException;
import com.nasya.ecommerce.entity.*;
import com.nasya.ecommerce.model.OrderStatus;
import com.nasya.ecommerce.model.request.checkout.CheckoutRequest;
import com.nasya.ecommerce.model.request.checkout.ShippingRateRequest;
import com.nasya.ecommerce.model.response.order.*;
import com.nasya.ecommerce.model.response.product.PaginatedProductResponse;
import com.nasya.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserAddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;

    private final BigDecimal TAX_RATE = BigDecimal.valueOf(0.03);
    private final ShippingService shippingService;
    private final PaymentService paymentService;
    private final XenditPaymentService xenditPaymentService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {

        //get item(product) that want to checkout from the cart
        List<CartItem> selectedItems = cartItemRepository.findAllById(request.getSelectedCartItemIds());
        //check if the cart is empty so checkout cannot be continue
        if(selectedItems.isEmpty()){
            throw new ResourceNotFoundException("cart item not found for checkout");
        }

        // get user address for shipping
        UserAddress shippingAddress = addressRepository.findById(request.getUserAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("shipping address not found"));

        // Mapping product ID and the quantities of the product
        Map<Long, Integer> productQuantities = selectedItems.stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));

        // check the quantities each product that want to add to the cart
        if(!inventoryService.checkAndLockInventory(productQuantities)){
            throw new InventoryException("Insufficeient inventory for one or more products");
        }

        // make new order object
        Order newOrder = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .taxFee(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .build();

        Order saveOrder = orderRepository.save(newOrder);

        // input all the item that want to checkout to order item
        List<OrderItem> orderItems = selectedItems.stream()
                .map(cartItem -> {
                    return OrderItem.builder()
                            .orderId(saveOrder.getOrderId())
                            .productId(cartItem.getProductId())
                            .quantity(cartItem.getQuantity())
                            .price(cartItem.getPrice())
                            .userAddressId(shippingAddress.getUserAddressId())
                            .build();
                }).toList();

        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteAll(selectedItems);

        BigDecimal subTotalAmount = orderItems.stream()
                .map(orderItem -> orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = orderItems.stream()
                .map(item -> {
                    Optional<Product> product = productRepository.findById(item.getProductId());
                    if (product.isEmpty()){
                        return BigDecimal.ZERO;
                    }
                    Optional<UserAddress> sellerAddress = userAddressRepository
                            .findByUserIdAndIsDefaultTrue(product.get().getUserId());
                    if(sellerAddress.isEmpty()){
                        return BigDecimal.ZERO;
                    }

                    BigDecimal totalWeight = product.get().getWeight().multiply(BigDecimal.valueOf(item.getQuantity()));
                    //calculate shipping rate
                    ShippingRateRequest rateRequest = ShippingRateRequest.builder()
                            .totalWeightInGrams(totalWeight)
                            .fromAddress(ShippingRateRequest.fromUserAddress(sellerAddress.get()))
                            .toAddress(ShippingRateRequest.fromUserAddress(shippingAddress))
                            .build();

                    ShippingRateResponse rateResponse =  shippingService.calculateShippingRate(rateRequest);
                    return rateResponse.getShippingFee();
                })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxFee = subTotalAmount.multiply(TAX_RATE);
        BigDecimal totalAmount = subTotalAmount.add(taxFee).add(shippingFee);
        saveOrder.setSubtotal(subTotalAmount);
        saveOrder.setShippingFee(shippingFee);
        saveOrder.setTotalAmount(totalAmount);
        orderRepository.save(saveOrder);

        //interact with xendit API
        //generate payment url
        String paymentUrl;

        try{
            PaymentResponse paymentResponse = paymentService.create(saveOrder);
            saveOrder.setXenditInvoiceId(paymentResponse.getXenditInvoiceId());
            saveOrder.setXenditPaymentStatus(paymentResponse.getXenditInvoiceStatus());
            paymentUrl = paymentResponse.getXenditPaymentUrl();
            orderRepository.save(saveOrder);

            inventoryService.decreaseQuantity(productQuantities);
        }catch (Exception e){
            log.error(e.getMessage());
            saveOrder.setStatus(OrderStatus.PAYMENT_FAILED);

            orderRepository.save(saveOrder);
            return OrderResponse.fromOrder(saveOrder);
        }

        OrderResponse orderResponse = OrderResponse.fromOrder(saveOrder);
        orderResponse.setXenditPaymentUrl(paymentUrl);

        return orderResponse;
    }

    @Override
    public Optional<Order> findOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> findOrderByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Page<OrderResponse> findOrderByUserIdAndPageable(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdByPageable(userId, pageable)
                .map(OrderResponse::fromOrder);
    }

    @Override
    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));

        if(!OrderStateTransition.isValidTransition(order.getStatus(), OrderStatus.CANCELLED)){
            throw new IllegalStateException("order cannot be cancelled");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        Map<Long, Integer> orderItemsQuantity =  orderItems.stream()
                        .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        if(order.getStatus().equals(OrderStatus.CANCELLED)){
        xenditPaymentService.cancelXenditInvoice(order);
        inventoryService.increaseQuantity(orderItemsQuantity);
        }
    }

    @Override
    public List<OrderItemResponse> findOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        if(orderItems.isEmpty()){
            throw new ResourceNotFoundException("order item not found for order");
        }

        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .toList();

        List<Long> shippingIds = orderItems.stream()
                .map(OrderItem::getUserAddressId)
                .toList();

        List<Product> products = productRepository.findAllById(productIds);
        List<UserAddress> shippingAddress = addressRepository.findAllById(shippingIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        Map<Long, UserAddress> shippingAddressMap = shippingAddress.stream()
                .collect(Collectors.toMap(UserAddress::getUserAddressId, Function.identity()));

        return orderItems.stream()
                .map(orderItem->{
                    Product product = productMap.get(orderItem.getProductId());
                    UserAddress userAddress = shippingAddressMap.get(orderItem.getUserAddressId());

                    if(product == null){
                        throw new ResourceNotFoundException("Product id not found");
                    }

                    if(userAddress == null){
                        throw new ResourceNotFoundException("shipping address not found");
                    }

                    return OrderItemResponse.fromOrderItemProductAndAddress(orderItem, product, userAddress);
                }).toList();
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));

        if(!OrderStateTransition.isValidTransition(order.getStatus(), newStatus)){
            throw new IllegalStateException("Order with current status: "+ order.getStatus() +
                    " Cannot be updated to: " + newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        if(newStatus.equals(OrderStatus.CANCELLED)){
            xenditPaymentService.cancelXenditInvoice(order);
            List<OrderItem> orderItems  = orderItemRepository.findByOrderId(orderId);
            Map<Long, Integer> orderItemsQuantity =  orderItems.stream()
                    .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));

            inventoryService.increaseQuantity(orderItemsQuantity);
        }
    }

    @Override
    public Double calculateOrderTotal(Long orderId) {
        return orderItemRepository.calculateTotalOrder(orderId);
    }

    @Override
    public PaginatedOrderResponse convertProductPage(Page<OrderResponse> response) {
        return PaginatedOrderResponse.builder()
                .data(response.getContent())
                .pageNo(response.getNumber())
                .pageSize(response.getSize())
                .totalElements(response.getNumberOfElements())
                .totalPages(response.getTotalPages())
                .build();
    }

    @Scheduled(cron = "0 0/1 * 1/1 * ?")
    @Transactional
    public void cancelUnpaidOrders(){
        LocalDateTime cancelThreshold = LocalDateTime.now().minusDays(1);
        List<Order> unpaidOrders = orderRepository
                .findByStatusAndOrderDateBefore(OrderStatus.PENDING, cancelThreshold);

        for(Order order: unpaidOrders){
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            xenditPaymentService.cancelXenditInvoice(order);
        }
    }
}
