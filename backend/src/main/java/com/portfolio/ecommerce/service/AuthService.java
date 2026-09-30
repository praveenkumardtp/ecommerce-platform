package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.request.LoginRequest;
import com.portfolio.ecommerce.dto.request.RegisterRequest;
import com.portfolio.ecommerce.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
