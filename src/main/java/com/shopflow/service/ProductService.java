package com.shopflow.service;

import com.shopflow.dto.request.CategoryRequest;
import com.shopflow.dto.request.ProductRequest;
import com.shopflow.dto.response.CategoryResponse;
import com.shopflow.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    // Produits
    ProductResponse createProduct(ProductRequest request, String sellerEmail);
    ProductResponse getProductById(Long id);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> searchProducts(String keyword, Pageable pageable);
    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);
    ProductResponse updateProduct(Long id, ProductRequest request, String sellerEmail);
    void deleteProduct(Long id, String sellerEmail);

    // Catégories
    CategoryResponse createCategory(CategoryRequest request);
    java.util.List<CategoryResponse> getAllCategories();
}