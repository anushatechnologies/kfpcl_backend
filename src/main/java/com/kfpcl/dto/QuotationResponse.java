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
public class QuotationResponse {

    private String id;
    private String rfqId;
    private SupplierSummaryDto supplier;
    private BigDecimal quotedPrice;
    private Integer quantity;
    private Integer leadTimeDays;
    private LocalDate validUntil;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
}
