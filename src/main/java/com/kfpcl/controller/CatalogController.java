package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.CategoryResponse;
import com.kfpcl.dto.PageResponse;
import com.kfpcl.dto.ProductDetailResponse;
import com.kfpcl.dto.ProductResponse;
import com.kfpcl.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @RequestParam(name = "isActive", required = false) Boolean isActive
    ) {
        List<CategoryResponse> categories = catalogService.getCategories(isActive);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<?>> getProducts(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "moqMax", required = false) Integer moqMax,
            @RequestParam(name = "gstVerified", required = false) Boolean gstVerified,
            @RequestParam(name = "verifiedSupplier", required = false) Boolean verifiedSupplier,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "featured", required = false) Boolean featured
    ) {
        // If featured=true is specifically requested as a flag without other pagination params
        if (Boolean.TRUE.equals(featured) && search == null && categoryId == null && minPrice == null && maxPrice == null && moqMax == null && gstVerified == null && verifiedSupplier == null) {
            List<ProductResponse> featuredProducts = catalogService.getFeaturedProducts();
            return ResponseEntity.ok(ApiResponse.success(featuredProducts));
        }

        PageResponse<ProductResponse> products = catalogService.getProducts(
                search, categoryId, minPrice, maxPrice, moqMax, gstVerified, verifiedSupplier, sortBy, page, limit, featured
        );
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
            @PathVariable("productId") String productId
    ) {
        ProductDetailResponse product = catalogService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
}
