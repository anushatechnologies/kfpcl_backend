package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ProductCreateDto;
import com.kfpcl.dto.ProductResponseDto;
import com.kfpcl.dto.ProductUpdateDto;
import com.kfpcl.dto.SellerProductCreateDto;

public interface ProductService {

    PageResponseDto<ProductResponseDto> getBuyerProducts(String search, String categoryId, String subcategoryId, String brand, Double minPrice, Double maxPrice, int page, int size, String sortBy, String sortDir);

    ProductResponseDto getBuyerProductById(String productId);

    PageResponseDto<ProductResponseDto> getAdminProducts(String search, String categoryId, String subcategoryId, String status, int page, int size, String sortBy, String sortDir);

    ProductResponseDto getAdminProductById(String productId);

    ProductResponseDto createProduct(ProductCreateDto dto);

    ProductResponseDto submitSellerProduct(SellerProductCreateDto dto);

    PageResponseDto<ProductResponseDto> getSellerProducts(String sellerId, String approvalStatus, int page, int size, String sortBy, String sortDir);

    ProductResponseDto updateProduct(String productId, ProductUpdateDto dto);

    void deleteProduct(String productId);
}
