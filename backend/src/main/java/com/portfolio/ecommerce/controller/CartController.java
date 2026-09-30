package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.dto.request.AddToCartRequest;
import com.portfolio.ecommerce.dto.request.UpdateCartItemRequest;
import com.portfolio.ecommerce.dto.response.CartResponse;
import com.portfolio.ecommerce.security.CustomUserDetails;
import com.portfolio.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Endpoints for managing customer cart items and quantities")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current user's shopping cart")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getId()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart with inventory stock check")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(userDetails.getId(), request));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity in cart")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(userDetails.getId(), itemId, request.getQuantity()));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeCartItem(userDetails.getId(), itemId));
    }

    @DeleteMapping
    @Operation(summary = "Clear shopping cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
