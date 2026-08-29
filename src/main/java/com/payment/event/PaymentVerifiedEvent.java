package com.payment.event;

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
public class PaymentVerifiedEvent {
    private String orderId;
    private Long transactionId;
    private String transactionReference;
    private String buyerId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime timestamp;
}
