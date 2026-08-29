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
public class QuotationResponseDto {

    private String id;
    private String rfqId;
    private String sellerId;
    private String sellerName;
    private Double unitPrice;
    private Double totalPrice;
    private LocalDateTime validUntil;
    private String status;
    private String terms;
    private Double freight;
    private Integer deliveryDays;
    private String timeline;
    private String paymentTerms;
    private String warranty;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
