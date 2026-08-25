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
public class SellerStoreResponseDto {

    private String sellerId;
    private String businessName;
    private String ownerName;
    private String email;
    private String phone;
    private String gstin;
    private String status;
    private int totalProducts;
    private long totalOrders;
    private double totalRevenue;
    private LocalDateTime joinedAt;
}
