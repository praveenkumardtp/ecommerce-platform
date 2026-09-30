package com.portfolio.ecommerce.dto.request;

import com.portfolio.ecommerce.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateRequest {

    @NotNull(message = "Order status is required (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)")
    private OrderStatus status;
}
