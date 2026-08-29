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
public class DispatchAllowedEvent {
    private String orderId;
    private Long transactionId;
    private String reason;
    private LocalDateTime timestamp;
}
