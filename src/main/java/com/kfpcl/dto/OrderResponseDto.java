package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {

    private String id;
    private String orderNumber;
    private String buyerId;
    private String buyerName;
    private String sellerId;
    private String sellerName;
    private Double totalAmount;
    private Double discountAmount;
    private Double taxAmount;
    private Double finalAmount;
    private String paymentStatus;
    private String orderStatus;
    private String shippingAddress;
    private String region;
    private List<OrderItemDto> items;
    private List<OrderTrackingResponseDto> trackingHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
