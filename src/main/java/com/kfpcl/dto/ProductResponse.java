package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String title;
    private String description;
    private String categoryId;
    private String categoryName;
    private SupplierSummaryDto supplier;
    private BigDecimal price;
    private String unit;
    private Integer moq;
    private Boolean featured;
    private String status;
    private String imageUrl;
}
