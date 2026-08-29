package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.request.SellerProfileRequest;
import com.kfpcl.dto.response.SellerProfileResponse;
import com.kfpcl.service.SellerProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/profile")
@RequiredArgsConstructor
public class SellerProfileController {

    private final SellerProfileService sellerProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<SellerProfileResponse>> getProfile(HttpServletRequest request) {
        String sellerId = (String) request.getAttribute("authenticatedUser");
        SellerProfileResponse response = sellerProfileService.getProfile(sellerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Seller profile retrieved successfully"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<SellerProfileResponse>> updateProfile(
            @RequestBody SellerProfileRequest requestDto,
            HttpServletRequest request) {
        
        String sellerId = (String) request.getAttribute("authenticatedUser");
        SellerProfileResponse response = sellerProfileService.updateProfile(sellerId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response, "Seller profile updated successfully"));
    }
}
