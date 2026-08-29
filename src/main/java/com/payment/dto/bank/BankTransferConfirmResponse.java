package com.payment.dto.bank;

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
public class BankTransferConfirmResponse {

    private String orderId;
    private String transactionReference;
    private String utrNumber;
    private String remitterBank;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus paymentStatus;
    private Boolean escrowLocked;
    private LocalDateTime transferDate;
    private LocalDateTime submittedAt;
    private String message;
}
