package com.payment.dto.refund;

import com.payment.entity.enums.EscrowReleaseStatus;
import com.payment.entity.enums.PaymentStatus;
import com.payment.entity.enums.RefundStatus;
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
public class RefundResponse {

    private Long refundId;
    private String refundReference;
    private String orderId;
    private Long transactionId;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private String currency;
    private RefundStatus refundStatus;
    private PaymentStatus paymentStatus;
    private EscrowReleaseStatus escrowReleaseStatus;
    private String gatewayRefundId;
    private String reason;
    private String approvedBy;
    private LocalDateTime completedAt;
    private String message;
}
