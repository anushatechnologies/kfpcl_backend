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
public class RfqSummaryResponse {

    private String id;
    private String productTitle;
    private Integer quantity;
    private String unit;
    private BigDecimal targetPrice;
    private LocalDate expectedDeliveryDate;
    private String status;
    private LocalDateTime createdAt;
}
