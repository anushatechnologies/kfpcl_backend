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
public class BuyerDetailResponseDto {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String status;
    private String region;
    private long totalOrders;
    private double totalSpent;
    private long totalRfqs;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
}
