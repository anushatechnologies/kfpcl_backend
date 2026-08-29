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
public class EscrowLockedEvent {
    private String orderId;
    private Long escrowAccountId;
    private String virtualAccountNumber;
    private BigDecimal totalAmountLocked;
    private LocalDateTime lockedAt;
}
