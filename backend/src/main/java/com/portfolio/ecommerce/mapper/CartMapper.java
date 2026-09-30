package com.portfolio.ecommerce.mapper;

import com.portfolio.ecommerce.dto.response.CartItemResponse;
import com.portfolio.ecommerce.dto.response.CartResponse;
import com.portfolio.ecommerce.dto.response.OrderItemResponse;
import com.portfolio.ecommerce.dto.response.OrderResponse;
import com.portfolio.ecommerce.entity.Cart;
import com.portfolio.ecommerce.entity.CartItem;
import com.portfolio.ecommerce.entity.Order;
import com.portfolio.ecommerce.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartItemResponse toCartItemResponse(CartItem item) {
        if (item == null) return null;
        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .unitPrice(item.getPrice())
                .quantity(item.getQuantity())
                .itemTotal(itemTotal)
                .availableStock(item.getProduct().getStockQuantity())
                .build();
    }

    public CartResponse toCartResponse(Cart cart) {
        if (cart == null) return null;
        List<CartItemResponse> items = cart.getItems() != null
                ? cart.getItems().stream().map(this::toCartItemResponse).collect(Collectors.toList())
                : Collections.emptyList();

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalCount = items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalPrice(totalPrice)
                .totalItems(totalCount)
                .build();
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        if (item == null) return null;
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : "Archived Item")
                .productImageUrl(item.getProduct() != null ? item.getProduct().getImageUrl() : null)
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) return null;
        List<OrderItemResponse> items = order.getItems() != null
                ? order.getItems().stream().map(this::toOrderItemResponse).collect(Collectors.toList())
                : Collections.emptyList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getUser() != null ? order.getUser().getId() : null)
                .customerName(order.getUser() != null ? order.getUser().getFullName() : null)
                .customerEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .paymentNotice(order.getPaymentNotice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
