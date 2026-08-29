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
public class FundsReleasedEvent {
    private String orderId;
    private String sellerId;
    private BigDecimal netAmount;
    private String bankReference;
    private LocalDateTime timestamp;
}
