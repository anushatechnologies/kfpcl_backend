package com.payment.dto.webhook;

import com.payment.entity.enums.EscrowReleaseStatus;
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
public class BankReconciliationWebhookResponse {

    private String eventId;
    private String orderId;
    private String transactionReference;
    private String utrNumber;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private EscrowReleaseStatus escrowStatus;
    private Boolean escrowLocked;
    private String invoiceNumber;
    private Boolean dispatchAuthorized;
    private LocalDateTime reconciledAt;
    private String message;
}
