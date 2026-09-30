package com.portfolio.ecommerce.service;

import com.portfolio.ecommerce.dto.request.CategoryRequest;
import com.portfolio.ecommerce.dto.request.ProductRequest;
import com.portfolio.ecommerce.dto.response.CategoryResponse;
import com.portfolio.ecommerce.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<ProductResponse> getProducts(String query, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    List<CategoryResponse> getAllCategories();
    CategoryResponse createCategory(CategoryRequest request);
}
