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
public class RefundCompletedEvent {
    private String orderId;
    private Long refundId;
    private String refundReference;
    private BigDecimal amount;
    private LocalDateTime timestamp;
}
