package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ProductResponseDto;
import com.kfpcl.dto.SellerProductCreateDto;
import com.kfpcl.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/seller/catalog/products", "/api/v1/seller/products"})
@RequiredArgsConstructor
public class SellerProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> submitProductForApproval(
            @Valid @RequestBody SellerProductCreateDto dto) {

        ProductResponseDto created = productService.submitSellerProduct(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Product submitted successfully and is pending admin approval"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<ProductResponseDto>>> listSellerProducts(
            @RequestParam(required = false) String sellerId,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @javax.servlet.http.HttpServletRequest request) {

        // If the client didn't pass a sellerId, fall back to the authenticated user from MockAuthInterceptor
        if (!org.springframework.util.StringUtils.hasText(sellerId)) {
            Object authUser = request.getAttribute("authenticatedUser");
            if (authUser != null) {
                sellerId = authUser.toString();
            }
        }

        PageResponseDto<ProductResponseDto> products = productService.getSellerProducts(
                sellerId, approvalStatus, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(products, "Seller products retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable String id,
            @RequestBody com.kfpcl.dto.ProductUpdateDto dto,
            jakarta.servlet.http.HttpServletRequest request) {
        ProductResponseDto updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivateProduct(
            @PathVariable String id,
            jakarta.servlet.http.HttpServletRequest request) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Success", "Product deactivated/archived successfully"));
    }
}
