package com.kfpcl.service;

import com.kfpcl.dto.request.SellerProfileRequest;
import com.kfpcl.dto.response.SellerProfileResponse;

public interface SellerProfileService {
    SellerProfileResponse getProfile(String sellerId);
    SellerProfileResponse updateProfile(String sellerId, SellerProfileRequest request);
}
