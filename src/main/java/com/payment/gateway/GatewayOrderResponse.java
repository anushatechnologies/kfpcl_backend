package com.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayOrderResponse {
    private String gatewayOrderId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Map<String, Object> rawResponse;
}
