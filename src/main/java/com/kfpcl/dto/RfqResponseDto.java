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
public class RfqResponseDto {

    private String id;
    private String rfqNumber;
    private String buyerId;
    private String buyerName;
    private String productId;
    private String productName;
    private Integer quantity;
    private Double targetPrice;
    private String status;
    private String deliveryLocation;
    private String notes;
    private LocalDateTime deadline;
    private int totalQuotations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
