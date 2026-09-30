package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.request.AddToCartRequest;
import com.portfolio.ecommerce.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId);
    CartResponse addToCart(Long userId, AddToCartRequest request);
    CartResponse updateCartItem(Long userId, Long itemId, int quantity);
    CartResponse removeCartItem(Long userId, Long itemId);
    void clearCart(Long userId);
}
