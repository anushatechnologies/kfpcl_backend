package com.payment.dto.gateway;

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
public class VerifyPaymentResponse {

    private String orderId;
    private String transactionReference;
    private String gatewayPaymentId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus paymentStatus;
    private Boolean escrowLocked;
    private EscrowReleaseStatus escrowStatus;
    private String virtualAccountNumber;
    private String invoiceNumber;
    private Boolean dispatchAllowed;
    private LocalDateTime verifiedAt;
    private String message;
}
