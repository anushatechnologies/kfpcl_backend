package com.kfpcl.dto.request;
import lombok.Data;

@Data
public class SellerProfileRequest {
    private String storeName;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String address;
}
