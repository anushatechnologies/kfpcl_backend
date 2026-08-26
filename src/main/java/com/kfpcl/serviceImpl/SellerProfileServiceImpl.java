package com.kfpcl.serviceImpl;

import com.kfpcl.dto.request.SellerProfileRequest;
import com.kfpcl.dto.response.SellerProfileResponse;
import com.kfpcl.entity.Seller;
import com.kfpcl.entity.enums.VerificationStatus;
import com.kfpcl.exception.DuplicateResourceException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.exception.UnverifiedSellerException;
import com.kfpcl.repository.SellerRepository;
import com.kfpcl.service.SellerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerProfileServiceImpl implements SellerProfileService {

    private final SellerRepository sellerRepository;

    @Override
    @Transactional(readOnly = true)
    public SellerProfileResponse getProfile(String userEmail) {
        Seller seller = getSellerEntityByEmail(userEmail);
        return mapToResponse(seller);
    }

    @Override
    @Transactional
    public SellerProfileResponse updateProfile(String userEmail, SellerProfileRequest request) {
        Seller seller = getSellerEntityByEmail(userEmail);

        String trimmedCompanyName = request.getCompanyName().trim();
        if (sellerRepository.existsByCompanyNameIgnoreCaseAndIdNot(trimmedCompanyName, seller.getId())) {
            throw new DuplicateResourceException("Seller", "companyName", trimmedCompanyName);
        }

        seller.setCompanyName(trimmedCompanyName);
        seller.setBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        seller.setTaxId(request.getTaxId());
        seller.setDescription(request.getDescription());
        seller.setLogoUrl(request.getLogoUrl());
        seller.setBannerUrl(request.getBannerUrl());
        seller.setAddress(request.getAddress());
        seller.setCity(request.getCity());
        seller.setState(request.getState());
        seller.setCountry(request.getCountry());
        seller.setPostalCode(request.getPostalCode());
        seller.setYearEstablished(request.getYearEstablished());

        Seller updatedSeller = sellerRepository.save(seller);
        return mapToResponse(updatedSeller);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerProfileResponse getSellerById(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "id", sellerId));
        return mapToResponse(seller);
    }

    @Override
    @Transactional(readOnly = true)
    public Seller getSellerEntityByEmail(String userEmail) {
        return sellerRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user: " + userEmail));
    }

    @Override
    @Transactional(readOnly = true)
    public Seller getVerifiedSellerEntity(String userEmail) {
        Seller seller = getSellerEntityByEmail(userEmail);
        if (!seller.isAllowedToPublish()) {
            throw new UnverifiedSellerException("Seller account is not verified. Unverified sellers cannot publish live products to the catalog.");
        }
        return seller;
    }

    @Override
    @Transactional
    public SellerProfileResponse updateVerificationStatus(Long sellerId, VerificationStatus status, boolean isVerified) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "id", sellerId));

        seller.setVerificationStatus(status);
        seller.setIsVerified(isVerified);

        Seller updatedSeller = sellerRepository.save(seller);
        return mapToResponse(updatedSeller);
    }

    private SellerProfileResponse mapToResponse(Seller seller) {
        if (seller == null) {
            return null;
        }

        return SellerProfileResponse.builder()
                .id(seller.getId())
                .userId(seller.getUser() != null ? seller.getUser().getId() : null)
                .email(seller.getUser() != null ? seller.getUser().getEmail() : null)
                .fullName(seller.getUser() != null ? seller.getUser().getFullName() : null)
                .phoneNumber(seller.getUser() != null ? seller.getUser().getPhoneNumber() : null)
                .companyName(seller.getCompanyName())
                .businessRegistrationNumber(seller.getBusinessRegistrationNumber())
                .taxId(seller.getTaxId())
                .description(seller.getDescription())
                .logoUrl(seller.getLogoUrl())
                .bannerUrl(seller.getBannerUrl())
                .address(seller.getAddress())
                .city(seller.getCity())
                .state(seller.getState())
                .country(seller.getCountry())
                .postalCode(seller.getPostalCode())
                .yearEstablished(seller.getYearEstablished())
                .rating(seller.getRating())
                .totalReviews(seller.getTotalReviews())
                .isVerified(seller.getIsVerified())
                .verificationStatus(seller.getVerificationStatus())
                .isAllowedToPublish(seller.isAllowedToPublish())
                .createdAt(seller.getCreatedAt())
                .updatedAt(seller.getUpdatedAt())
                .build();
    }
}
