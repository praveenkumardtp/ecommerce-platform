package com.portfolio.ecommerce.exception;

public class ResourceNotFoundException extends EcommerceException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s : '%s'", resource, field, value));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
