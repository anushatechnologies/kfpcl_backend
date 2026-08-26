package com.kfpcl.controller;

import com.kfpcl.dto.request.SellerProfileRequest;
import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.SellerProfileResponse;
import com.kfpcl.service.SellerProfileService;
import com.kfpcl.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/seller/profile")
@RequiredArgsConstructor
public class SellerProfileController {

    private final SellerProfileService sellerProfileService;

    /**
     * Protected API: View logged-in seller storefront configuration.
     * GET /api/v1/seller/profile
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getProfile(Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        SellerProfileResponse profile = sellerProfileService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Seller profile retrieved successfully", profile));
    }

    /**
     * Protected API: Update logged-in seller storefront configuration.
     * PUT /api/v1/seller/profile
     */
    @PutMapping
    public ResponseEntity<ApiResponse<SellerProfileResponse>> updateProfile(
            @Valid @RequestBody SellerProfileRequest request,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        SellerProfileResponse updatedProfile = sellerProfileService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success("Seller profile updated successfully", updatedProfile));
    }
}
