package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavoriteResponse {

    private Long favoriteId;
    private Long productId;
    private String productName;
    private String productSlug;
    private BigDecimal basePrice;
    private Integer moq;
    private String unit;
    private String primaryImageUrl;
    private Long sellerId;
    private String sellerCompanyName;
    private Boolean sellerIsVerified;
    private LocalDateTime savedAt;
}
