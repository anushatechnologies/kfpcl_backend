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
public class ProductApprovalResponseDto {

    private String approvalId;
    private String productId;
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
    private String sku;
    private String status;
    private String approvalStatus;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
