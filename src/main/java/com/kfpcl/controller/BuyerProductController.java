package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ProductResponseDto;
import com.kfpcl.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/catalog/products", "/api/v1/products"})
@RequiredArgsConstructor
public class BuyerProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<ProductResponseDto>>> listBuyerProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String subcategoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer maxMoq,
            @RequestParam(required = false) Boolean verifiedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<ProductResponseDto> products = productService.getBuyerProducts(
                search, categoryId, subcategoryId, brand, minPrice, maxPrice, maxMoq, verifiedOnly, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(products, "Buyer products retrieved successfully"));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getBuyerProductDetails(
            @PathVariable String productId) {

        ProductResponseDto product = productService.getBuyerProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product, "Product details retrieved successfully"));
    }
}
