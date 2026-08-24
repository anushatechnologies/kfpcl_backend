package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ProductCreateDto;
import com.kfpcl.dto.ProductResponseDto;
import com.kfpcl.dto.ProductUpdateDto;
import com.kfpcl.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/catalog/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<ProductResponseDto>>> listAdminProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String subcategoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<ProductResponseDto> products = productService.getAdminProducts(
                search, categoryId, subcategoryId, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(products, "Admin products retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(
            @Valid @RequestBody ProductCreateDto dto) {

        ProductResponseDto created = productService.createProduct(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Product created successfully"));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getAdminProduct(
            @PathVariable String productId) {

        ProductResponseDto product = productService.getAdminProductById(productId);
        return ResponseEntity.ok(ApiResponse.success(product, "Admin product details retrieved successfully"));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable String productId,
            @RequestBody ProductUpdateDto dto) {

        ProductResponseDto updated = productService.updateProduct(productId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Product updated successfully"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable String productId) {

        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted/archived successfully"));
    }
}
