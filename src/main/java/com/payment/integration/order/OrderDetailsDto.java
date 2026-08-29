package com.payment.integration.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDto {
    private String orderId;
    private String buyerId;
    private String sellerId;
    private BigDecimal grandTotal;
    private String currency;
    private String orderStatus;
    private String paymentStatus;
    private String customerEmail;
}
