package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private String id;
    private String productName;
    private String categoryId;
    private String categoryName;
    private String subcategoryId;
    private String subcategoryName;
    private String brand;
    private String description;
    private String imageUrl;
    private Double price;
    private Double mrp;
    private Double quantity;
    private String unit;
    private Integer stockQuantity;
    private String status;
    private String approvalStatus;
    private String rejectionReason;
    private String sellerId;
    private String createdBy;
    private String sku;
    private Double discount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
