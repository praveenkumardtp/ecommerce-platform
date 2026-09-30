package com.portfolio.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalOrders;
    private long totalCustomers;
    private long totalProducts;
    private long lowStockProducts;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long deliveredOrders;
}
