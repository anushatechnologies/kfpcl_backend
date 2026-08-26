package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kfpcl.entity.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SellerProfileResponse {

    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String companyName;
    private String businessRegistrationNumber;
    private String taxId;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Integer yearEstablished;
    private Double rating;
    private Integer totalReviews;
    private Boolean isVerified;
    private VerificationStatus verificationStatus;
    private Boolean isAllowedToPublish;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
