package com.portfolio.ecommerce.service.impl;

import com.portfolio.ecommerce.dto.response.DashboardStatsResponse;
import com.portfolio.ecommerce.dto.response.ProductResponse;
import com.portfolio.ecommerce.entity.OrderStatus;
import com.portfolio.ecommerce.entity.Product;
import com.portfolio.ecommerce.exception.ResourceNotFoundException;
import com.portfolio.ecommerce.mapper.ProductMapper;
import com.portfolio.ecommerce.repository.OrderRepository;
import com.portfolio.ecommerce.repository.ProductRepository;
import com.portfolio.ecommerce.repository.UserRepository;
import com.portfolio.ecommerce.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalOrders = orderRepository.count();
        long totalCustomers = userRepository.count();
        long totalProducts = productRepository.count();
        long lowStock = productRepository.countByStockQuantityLessThan(5);
        BigDecimal totalRevenue = orderRepository.sumTotalSales();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);

        return DashboardStatsResponse.builder()
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStock)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .pendingOrders(pendingOrders)
                .deliveredOrders(deliveredOrders)
                .build();
    }

    @Override
    @Transactional
    public ProductResponse updateInventory(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.setStockQuantity(Math.max(0, quantity));
        return productMapper.toResponse(productRepository.save(product));
    }
}
