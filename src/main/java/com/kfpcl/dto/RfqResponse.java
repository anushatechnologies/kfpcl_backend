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
public class RfqResponse {

    private String id;
    private String buyerId;
    private String productId;
    private String productTitle;
    private String categoryId;
    private String categoryName;
    private Integer quantity;
    private String unit;
    private BigDecimal targetPrice;
    private LocalDate expectedDeliveryDate;
    private String description;
    private String status;
    private int quotationsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
