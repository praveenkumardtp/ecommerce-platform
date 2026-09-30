package com.portfolio.ecommerce.controller;

import com.portfolio.ecommerce.dto.request.CategoryRequest;
import com.portfolio.ecommerce.dto.request.OrderStatusUpdateRequest;
import com.portfolio.ecommerce.dto.request.ProductRequest;
import com.portfolio.ecommerce.dto.response.CategoryResponse;
import com.portfolio.ecommerce.dto.response.DashboardStatsResponse;
import com.portfolio.ecommerce.dto.response.OrderResponse;
import com.portfolio.ecommerce.dto.response.ProductResponse;
import com.portfolio.ecommerce.service.AdminService;
import com.portfolio.ecommerce.service.OrderService;
import com.portfolio.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Endpoints for platform administration, inventory, product CRUD, and order management")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get platform metrics: sales, orders, products, inventory warnings")
    public ResponseEntity<DashboardStatsResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @PostMapping("/products")
    @Operation(summary = "Add a new product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Soft delete / deactivate a product")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categories")
    @Operation(summary = "Create product category")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return new ResponseEntity<>(productService.createCategory(request), HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}/inventory")
    @Operation(summary = "Adjust inventory stock for a product")
    public ResponseEntity<ProductResponse> updateInventory(
            @PathVariable Long id,
            @RequestParam int quantity) {
        return ResponseEntity.ok(adminService.updateInventory(id, quantity));
    }

    @GetMapping("/orders")
    @Operation(summary = "View all customer orders")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Update order status: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }
}
