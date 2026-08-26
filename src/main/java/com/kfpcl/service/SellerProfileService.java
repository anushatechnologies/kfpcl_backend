package com.kfpcl.service;

import com.kfpcl.dto.request.SellerProfileRequest;
import com.kfpcl.dto.response.SellerProfileResponse;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.VerificationStatus;

public interface SellerProfileService {

    /**
     * Retrieves the profile configuration of the logged-in seller.
     */
    SellerProfileResponse getProfile(String userEmail);

    /**
     * Updates the storefront profile of the logged-in seller.
     */
    SellerProfileResponse updateProfile(String userEmail, SellerProfileRequest request);

    /**
     * Retrieves seller profile by Seller ID (used by public storefront).
     */
    SellerProfileResponse getSellerById(Long sellerId);

    /**
     * Internal helper to retrieve Seller entity by user email.
     */
    Seller getSellerEntityByEmail(String userEmail);

    /**
     * Validates that seller exists and has VERIFIED status before allowing product publishing.
     */
    Seller getVerifiedSellerEntity(String userEmail);

    /**
     * Admin workflow to approve/reject seller KYC status.
     */
    SellerProfileResponse updateVerificationStatus(Long sellerId, VerificationStatus status, boolean isVerified);
}
