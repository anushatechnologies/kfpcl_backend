package com.payment.dto.escrow;

import com.payment.entity.enums.EscrowReleaseStatus;
import com.payment.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualAccountResponse {

    private String orderId;
    private String beneficiaryName;
    private String virtualAccountNumber;
    private String ifscCode;
    private String bankName;
    private BigDecimal amount;
    private String currency;
    private EscrowReleaseStatus escrowStatus;
    private PaymentStatus paymentStatus;
    private String instructions;
}
