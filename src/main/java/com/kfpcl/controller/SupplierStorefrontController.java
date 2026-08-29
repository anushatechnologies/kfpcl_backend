package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.response.SellerProfileResponse;
import com.kfpcl.dto.response.SupplierStorefrontResponse;
import com.kfpcl.service.SellerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierStorefrontController {

    private final SellerProfileService sellerProfileService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierStorefrontResponse>> getSupplierStorefront(@PathVariable String id) {
        SellerProfileResponse profile = sellerProfileService.getProfile(id);
        
        SupplierStorefrontResponse storefront = SupplierStorefrontResponse.builder()
                .id(profile.getId())
                .storeName(profile.getStoreName())
                .description(profile.getDescription())
                .logoUrl(profile.getLogoUrl())
                .bannerUrl(profile.getBannerUrl())
                .address(profile.getAddress())
                .verificationStatus(profile.getVerificationStatus())
                .recentProducts(Collections.emptyList())
                .build();
                
        return ResponseEntity.ok(ApiResponse.success(storefront, "Supplier storefront retrieved successfully"));
    }
}
