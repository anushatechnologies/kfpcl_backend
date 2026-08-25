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
public class LatestSaleDto {

    private String orderId;
    private String orderNumber;
    private String customerName;
    private String sellerName;
    private Double amount;
    private String status;
    private LocalDateTime timestamp;
}
