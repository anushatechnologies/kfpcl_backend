package com.kfpcl.service;

import com.kfpcl.dto.CategoryResponse;
import com.kfpcl.dto.PageResponse;
import com.kfpcl.dto.ProductDetailResponse;
import com.kfpcl.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CatalogService {

    List<CategoryResponse> getCategories(Boolean isActive);

    List<ProductResponse> getFeaturedProducts();

    PageResponse<ProductResponse> getProducts(
            String search,
            String categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer moqMax,
            Boolean gstVerified,
            Boolean verifiedSupplier,
            String sortBy,
            int page,
            int limit,
            Boolean featured
    );

    ProductDetailResponse getProductById(String productId);
}
