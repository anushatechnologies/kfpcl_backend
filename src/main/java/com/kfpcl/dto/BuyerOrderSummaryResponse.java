package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerOrderSummaryResponse {

    private String id;
    private String supplierName;
    private BigDecimal totalAmount;
    private String status;
    private int totalItems;
    private LocalDate estimatedDelivery;
    private LocalDateTime createdAt;
}
