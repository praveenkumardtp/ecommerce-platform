package com.portfolio.ecommerce.exception;

public class InsufficientStockException extends EcommerceException {
    public InsufficientStockException(String productName, int available, int requested) {
        super(String.format("Insufficient inventory for product '%s'. In stock: %d, Requested: %d",
                productName, available, requested));
    }
}
