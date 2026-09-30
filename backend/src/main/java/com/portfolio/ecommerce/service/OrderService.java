package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.request.CreateOrderRequest;
import com.portfolio.ecommerce.dto.request.OrderStatusUpdateRequest;
import com.portfolio.ecommerce.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(Long userId, CreateOrderRequest request);
    Page<OrderResponse> getOrderHistory(Long userId, Pageable pageable);
    OrderResponse getOrderById(Long userId, Long orderId);
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
    Page<OrderResponse> getAllOrders(Pageable pageable);
}
