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
public class GatewayRefundResponse {
    private String gatewayRefundId;
    private String gatewayPaymentId;
    private BigDecimal amount;
    private String status;
}
