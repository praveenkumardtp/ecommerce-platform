package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.response.DashboardStatsResponse;
import com.portfolio.ecommerce.dto.response.ProductResponse;

public interface AdminService {
    DashboardStatsResponse getDashboardStats();
    ProductResponse updateInventory(Long productId, int quantity);
}
