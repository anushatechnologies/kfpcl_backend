package com.payment.dto.bank;

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
public class BankTransferConfirmRequest {

    @NotBlank(message = "Order ID is mandatory")
    private String orderId;

    @NotBlank(message = "UTR number is mandatory")
    private String utrNumber;

    @NotBlank(message = "Remitter bank is mandatory")
    private String remitterBank;

    @NotNull(message = "Transfer date is mandatory")
    private LocalDateTime transferDate;

    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String receiptDocUrl;
}
