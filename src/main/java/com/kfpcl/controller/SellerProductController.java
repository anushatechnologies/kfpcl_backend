package com.kfpcl.controller;

import com.kfpcl.dto.request.ProductCreateRequest;
import com.kfpcl.dto.request.ProductUpdateRequest;
import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.ProductResponse;
import com.kfpcl.entity.enums.ProductStatus;
import com.kfpcl.service.SellerProductService;
import com.kfpcl.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductService sellerProductService;

    /**
     * Protected API: Create a new product.
     * POST /api/v1/seller/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        ProductResponse createdProduct = sellerProductService.createProduct(email, request);
        return new ResponseEntity<>(
                ApiResponse.success("Product created successfully", createdProduct),
                HttpStatus.CREATED
        );
    }

    /**
     * Protected API: List seller's products with pagination and optional status filter.
     * GET /api/v1/seller/products
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getSellerProducts(
            @RequestParam(value = "status", required = false) ProductStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        PageResponse<ProductResponse> products = sellerProductService.getSellerProducts(
                email, status, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success("Seller products retrieved successfully", products));
    }

    /**
     * Protected API: Get single seller product details.
     * GET /api/v1/seller/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getSellerProductById(
            @PathVariable Long id,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        ProductResponse product = sellerProductService.getSellerProductById(email, id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    /**
     * Protected API: Update product, inventory, specs, and pricing tiers.
     * PUT /api/v1/seller/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        ProductResponse updatedProduct = sellerProductService.updateProduct(email, id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
    }

    /**
     * Protected API: Deactivate / Archive product.
     * DELETE /api/v1/seller/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        sellerProductService.deleteProduct(email, id);
        return ResponseEntity.ok(ApiResponse.success("Product archived successfully", null));
    }
}
