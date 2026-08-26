package com.kfpcl.controller;

import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.dto.response.ProductResponse;
import com.kfpcl.dto.response.SupplierStorefrontResponse;
import com.kfpcl.service.SupplierStorefrontService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierStorefrontController {

    private final SupplierStorefrontService supplierStorefrontService;

    /**
     * Public API: View public supplier storefront profile & featured products.
     * GET /api/v1/suppliers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierStorefrontResponse>> getStorefrontById(@PathVariable Long id) {
        SupplierStorefrontResponse storefront = supplierStorefrontService.getStorefrontById(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier storefront retrieved successfully", storefront));
    }

    /**
     * Public API: Paginate live products published by this supplier.
     * GET /api/v1/suppliers/{id}/products
     */
    @GetMapping("/{id}/products")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getSupplierProducts(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir) {

        PageResponse<ProductResponse> products = supplierStorefrontService.getSupplierProducts(
                id, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.success("Supplier products retrieved successfully", products));
    }
}
