package com.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRefundRequest {
    private String paymentId;
    private BigDecimal amount;
    private String currency;
    private String reason;
}
