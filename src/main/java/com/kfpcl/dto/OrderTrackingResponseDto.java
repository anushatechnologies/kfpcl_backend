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
public class OrderTrackingResponseDto {

    private String id;
    private String orderId;
    private String carrier;
    private String trackingNumber;
    private String status;
    private String location;
    private String remarks;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdAt;
}
