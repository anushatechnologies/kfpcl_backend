package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerOrderResponse {

    private String id;
    private String buyerId;
    private SupplierSummaryDto supplier;
    private String rfqId;
    private String quotationId;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    private String trackingNumber;
    private String courierPartner;
    private LocalDate estimatedDelivery;
    private LocalDateTime deliveredAt;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
