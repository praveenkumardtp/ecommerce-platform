package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.dto.request.CreateOrderRequest;
import com.portfolio.ecommerce.dto.response.OrderResponse;
import com.portfolio.ecommerce.security.CustomUserDetails;
import com.portfolio.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Customer Orders", description = "Endpoints for creating and tracking customer orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Checkout cart and create order", description = "Deducts stock atomically, creates order with 'Payment integration not configured' status")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(userDetails.getId(), request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get user order history with pagination")
    public ResponseEntity<Page<OrderResponse>> getOrderHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getOrderHistory(userDetails.getId(), pageable));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get specific order details")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(userDetails.getId(), orderId));
    }
}
