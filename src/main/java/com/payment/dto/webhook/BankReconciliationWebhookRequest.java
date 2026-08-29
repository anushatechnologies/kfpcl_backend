package com.payment.dto.webhook;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class BankReconciliationWebhookRequest {

    @NotBlank(message = "Bank Event ID is mandatory for replay protection")
    private String eventId;

    @NotBlank(message = "Virtual Account Number is mandatory")
    private String virtualAccountNumber;

    @NotBlank(message = "UTR Number is mandatory")
    private String utrNumber;

    @NotNull(message = "Reconciliation amount is mandatory")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Credit timestamp is mandatory")
    private LocalDateTime creditTimestamp;

    private String remitterAccount;
    private String remitterName;
}
