package com.kfpcl.dto;

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
public class ProductDetailResponse {

    private String id;
    private String title;
    private String description;
    private String categoryId;
    private String categoryName;
    private SupplierSummaryDto supplier;
    private BigDecimal price;
    private String unit;
    private Integer moq;
    private Integer stockQuantity;
    private Boolean featured;
    private String status;
    private String imageUrl;
    private BigDecimal gstRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
