package com.payment.dto.gateway;

import com.payment.entity.enums.PaymentGatewayType;
import com.payment.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGatewayOrderResponse {

    private String transactionReference;
    private String orderId;
    private String gatewayOrderId;
    private PaymentGatewayType gateway;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String keyId;
    private String idempotencyKey;
    private LocalDateTime createdAt;
}
