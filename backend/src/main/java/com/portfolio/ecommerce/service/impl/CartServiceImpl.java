package com.portfolio.ecommerce.service.impl;

import com.portfolio.ecommerce.dto.request.AddToCartRequest;
import com.portfolio.ecommerce.dto.response.CartResponse;
import com.portfolio.ecommerce.entity.Cart;
import com.portfolio.ecommerce.entity.CartItem;
import com.portfolio.ecommerce.entity.Product;
import com.portfolio.ecommerce.entity.User;
import com.portfolio.ecommerce.exception.BadRequestException;
import com.portfolio.ecommerce.exception.InsufficientStockException;
import com.portfolio.ecommerce.exception.ResourceNotFoundException;
import com.portfolio.ecommerce.mapper.CartMapper;
import com.portfolio.ecommerce.repository.CartItemRepository;
import com.portfolio.ecommerce.repository.CartRepository;
import com.portfolio.ecommerce.repository.ProductRepository;
import com.portfolio.ecommerce.repository.UserRepository;
import com.portfolio.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.getActive()) {
            throw new BadRequestException("Product is inactive or discontinued.");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        int targetQuantity = request.getQuantity();
        if (existingItem.isPresent()) {
            targetQuantity += existingItem.get().getQuantity();
        }

        if (product.getStockQuantity() < targetQuantity) {
            throw new InsufficientStockException(product.getName(), product.getStockQuantity(), targetQuantity);
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(targetQuantity);
            item.setPrice(product.getPrice());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        Cart updatedCart = cartRepository.findById(cart.getId()).orElse(cart);
        return cartMapper.toCartResponse(updatedCart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Unauthorized access to cart item");
        }

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new InsufficientStockException(item.getProduct().getName(), item.getProduct().getStockQuantity(), quantity);
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return cartMapper.toCartResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(Long userId, Long itemId) {
        return updateCartItem(userId, itemId, 0);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }
}
