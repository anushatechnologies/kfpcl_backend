package com.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDisputedEvent {
    private String orderId;
    private Long disputeId;
    private String reason;
    private String raisedByUserId;
    private LocalDateTime timestamp;
}
