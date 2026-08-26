package com.kfpcl.controller;

import com.kfpcl.dto.request.ProductFilterRequest;
import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.ProductResponse;
import com.kfpcl.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCatalogService productCatalogService;

    /**
     * Public API: Search, filter, and paginate catalog products.
     * GET /api/v1/products
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "maxMoq", required = false) Integer maxMoq,
            @RequestParam(value = "unit", required = false) String unit,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir) {

        ProductFilterRequest filter = ProductFilterRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .maxMoq(maxMoq)
                .unit(unit)
                .build();

        PageResponse<ProductResponse> response = productCatalogService.searchProducts(
                filter, page, size, sortBy, sortDir
        );

        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", response));
    }

    /**
     * Public API: Get product details and volume pricing tiers by Product ID.
     * GET /api/v1/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetailsById(@PathVariable Long id) {
        ProductResponse product = productCatalogService.getProductDetailsById(id);
        return ResponseEntity.ok(ApiResponse.success("Product details retrieved successfully", product));
    }

    /**
     * Public API: Get product details by SEO slug.
     * GET /api/v1/products/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductDetailsBySlug(@PathVariable String slug) {
        ProductResponse product = productCatalogService.getProductDetailsBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Product details retrieved successfully", product));
    }
}
