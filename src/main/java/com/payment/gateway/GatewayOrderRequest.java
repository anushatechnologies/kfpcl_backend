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
public class GatewayOrderRequest {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String receipt;
    private Map<String, String> notes;
}
