package com.kfpcl.serviceImpl;

import com.kfpcl.dto.request.SellerProfileRequest;
import com.kfpcl.dto.response.SellerProfileResponse;
import com.kfpcl.entity.SellerApplication;
import com.kfpcl.entity.SellerProfile;
import com.kfpcl.entity.User;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.SellerApplicationRepository;
import com.kfpcl.repository.SellerProfileRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.SellerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SellerProfileServiceImpl implements SellerProfileService {

    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;
    private final SellerApplicationRepository sellerApplicationRepository;

    @Override
    @Transactional(readOnly = true)
    public SellerProfileResponse getProfile(String sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        SellerProfile profile = sellerProfileRepository.findByUserId(sellerId)
                .orElseGet(() -> {
                    // Create a default profile if it doesn't exist
                    return SellerProfile.builder()
                            .id(UUID.randomUUID().toString())
                            .userId(sellerId)
                            .storeName(seller.getName() + " Store")
                            .build();
                });

        SellerApplication app = sellerApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(sellerId).orElse(null);
        String verificationStatus = app != null ? app.getStatus().name() : "UNVERIFIED";

        return mapToResponse(profile, verificationStatus);
    }

    @Override
    @Transactional
    public SellerProfileResponse updateProfile(String sellerId, SellerProfileRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        SellerProfile profile = sellerProfileRepository.findByUserId(sellerId)
                .orElse(SellerProfile.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(sellerId)
                        .build());

        profile.setStoreName(request.getStoreName());
        profile.setDescription(request.getDescription());
        profile.setLogoUrl(request.getLogoUrl());
        profile.setBannerUrl(request.getBannerUrl());
        profile.setAddress(request.getAddress());

        profile = sellerProfileRepository.save(profile);

        SellerApplication app = sellerApplicationRepository.findFirstByUserIdOrderByCreatedAtDesc(sellerId).orElse(null);
        String verificationStatus = app != null ? app.getStatus().name() : "UNVERIFIED";

        return mapToResponse(profile, verificationStatus);
    }

    private SellerProfileResponse mapToResponse(SellerProfile profile, String verificationStatus) {
        return SellerProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .storeName(profile.getStoreName())
                .description(profile.getDescription())
                .logoUrl(profile.getLogoUrl())
                .bannerUrl(profile.getBannerUrl())
                .address(profile.getAddress())
                .verificationStatus(verificationStatus)
                .build();
    }
}
