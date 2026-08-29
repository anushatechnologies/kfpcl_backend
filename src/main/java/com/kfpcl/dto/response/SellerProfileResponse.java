package com.kfpcl.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileResponse {
    private String id;
    private String userId;
    private String storeName;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String address;
    private String verificationStatus;
}
