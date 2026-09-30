package com.portfolio.ecommerce.service.impl;

import com.portfolio.ecommerce.dto.request.LoginRequest;
import com.portfolio.ecommerce.dto.request.RegisterRequest;
import com.portfolio.ecommerce.dto.response.AuthResponse;
import com.portfolio.ecommerce.entity.Cart;
import com.portfolio.ecommerce.entity.Role;
import com.portfolio.ecommerce.entity.RoleType;
import com.portfolio.ecommerce.entity.User;
import com.portfolio.ecommerce.exception.BadRequestException;
import com.portfolio.ecommerce.exception.ResourceNotFoundException;
import com.portfolio.ecommerce.repository.CartRepository;
import com.portfolio.ecommerce.repository.RoleRepository;
import com.portfolio.ecommerce.repository.UserRepository;
import com.portfolio.ecommerce.security.CustomUserDetails;
import com.portfolio.ecommerce.security.JwtTokenProvider;
import com.portfolio.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        Role customerRole = roleRepository.findByName(RoleType.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ROLE_CUSTOMER).build()));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(customerRole)
                .build();

        User savedUser = userRepository.save(user);

        // Auto-provision personal shopping cart
        Cart cart = Cart.builder().user(savedUser).build();
        cartRepository.save(cart);

        String jwt = tokenProvider.generateTokenFromUsername(savedUser.getEmail());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().getName().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .build();
    }
}
