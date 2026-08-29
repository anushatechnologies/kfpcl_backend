package com.kfpcl.dto.response;

import com.kfpcl.dto.ProductResponseDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SupplierStorefrontResponse {
    private String id;
    private String storeName;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String address;
    private String verificationStatus;
    private List<ProductResponseDto> recentProducts;
}
