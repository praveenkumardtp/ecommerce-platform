package com.portfolio.ecommerce.service.impl;

import com.portfolio.ecommerce.dto.request.CategoryRequest;
import com.portfolio.ecommerce.dto.request.ProductRequest;
import com.portfolio.ecommerce.dto.response.CategoryResponse;
import com.portfolio.ecommerce.dto.response.ProductResponse;
import com.portfolio.ecommerce.entity.Category;
import com.portfolio.ecommerce.entity.Product;
import com.portfolio.ecommerce.exception.BadRequestException;
import com.portfolio.ecommerce.exception.ResourceNotFoundException;
import com.portfolio.ecommerce.mapper.ProductMapper;
import com.portfolio.ecommerce.repository.CategoryRepository;
import com.portfolio.ecommerce.repository.ProductRepository;
import com.portfolio.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String query, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        String sanitizedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        return productRepository.filterProducts(sanitizedQuery, categoryId, minPrice, maxPrice, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl() != null && !request.getImageUrl().isBlank()
                        ? request.getImageUrl() : "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=500")
                .category(category)
                .active(true)
                .build();

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false); // Soft-delete for order integrity
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(productMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Category already exists: " + request.getName());
        }
        Category cat = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();
        return productMapper.toCategoryResponse(categoryRepository.save(cat));
    }
}
