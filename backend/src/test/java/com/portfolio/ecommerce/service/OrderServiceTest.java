package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.request.CreateOrderRequest;
import com.portfolio.ecommerce.dto.request.OrderStatusUpdateRequest;
import com.portfolio.ecommerce.dto.response.OrderResponse;
import com.portfolio.ecommerce.entity.*;
import com.portfolio.ecommerce.exception.BadRequestException;
import com.portfolio.ecommerce.exception.InsufficientStockException;
import com.portfolio.ecommerce.mapper.CartMapper;
import com.portfolio.ecommerce.repository.*;
import com.portfolio.ecommerce.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private CartMapper cartMapper = new CartMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private User customer;
    private Product headphone;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .fullName("Praveen Kumar")
                .email("customer@store.com")
                .build();

        headphone = Product.builder()
                .id(10L)
                .name("Wireless Noise-Cancelling Headphones")
                .price(new BigDecimal("200.00"))
                .stockQuantity(10)
                .active(true)
                .build();

        cart = Cart.builder()
                .id(100L)
                .user(customer)
                .items(new ArrayList<>())
                .build();

        cartItem = CartItem.builder()
                .id(50L)
                .cart(cart)
                .product(headphone)
                .quantity(2)
                .price(headphone.getPrice())
                .build();

        cart.getItems().add(cartItem);
    }

    @Test
    @DisplayName("Create Order: Success - decrements inventory, clears cart, and records required payment notice")
    void testCreateOrder_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(999L);
            return o;
        });

        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("123 Tech Park, San Francisco, CA")
                .build();

        OrderResponse response = orderService.createOrder(1L, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("400.00"), response.getTotalAmount());
        assertEquals("Payment integration not configured", response.getPaymentNotice());
        assertEquals(8, headphone.getStockQuantity()); // 10 - 2 = 8
        assertEquals(OrderStatus.PENDING, response.getStatus());

        verify(productRepository, times(1)).save(headphone);
        verify(cartItemRepository, times(1)).deleteByCartId(100L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Create Order: Insufficient stock throws InsufficientStockException and does not decrement stock")
    void testCreateOrder_InsufficientStock_ThrowsException() {
        headphone.setStockQuantity(1); // Cart requires 2

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("123 Tech Park")
                .build();

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(1L, request));
        assertEquals(1, headphone.getStockQuantity()); // unchanged
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Create Order: Empty cart throws BadRequestException")
    void testCreateOrder_EmptyCart_ThrowsException() {
        cart.getItems().clear();

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("123 Tech Park")
                .build();

        assertThrows(BadRequestException.class, () -> orderService.createOrder(1L, request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Cancel Order: Restores product inventory stock")
    void testCancelOrder_RestoresInventory() {
        headphone.setStockQuantity(8);

        OrderItem orderItem = OrderItem.builder()
                .product(headphone)
                .quantity(2)
                .unitPrice(new BigDecimal("200.00"))
                .subtotal(new BigDecimal("400.00"))
                .build();

        Order existingOrder = Order.builder()
                .id(999L)
                .orderNumber("ORD-999")
                .user(customer)
                .status(OrderStatus.PENDING)
                .items(List.of(orderItem))
                .build();

        when(orderRepository.findById(999L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderStatusUpdateRequest updateRequest = OrderStatusUpdateRequest.builder()
                .status(OrderStatus.CANCELLED)
                .build();

        OrderResponse response = orderService.updateOrderStatus(999L, updateRequest);

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals(10, headphone.getStockQuantity()); // 8 + 2 restored to 10
        verify(productRepository, times(1)).save(headphone);
    }
}
