package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kfpcl.entity.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private Long id;
    private Long sellerId;
    private String sellerCompanyName;
    private Boolean sellerIsVerified;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private BigDecimal basePrice;
    private Integer moq;
    private Integer stockQuantity;
    private String unit;
    private ProductStatus status;
    private Boolean isApproved;
    private String primaryImageUrl;
    private List<String> imageUrls;
    private Map<String, Object> specifications;
    private List<ProductPriceTierResponse> priceTiers;
    private String tags;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
