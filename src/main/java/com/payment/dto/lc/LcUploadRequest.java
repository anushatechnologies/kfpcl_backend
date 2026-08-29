package com.payment.dto.lc;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LcUploadRequest {

    @NotBlank(message = "Order ID is mandatory")
    private String orderId;

    @NotBlank(message = "LC number is mandatory")
    private String lcNumber;

    @NotBlank(message = "Issuing bank is mandatory")
    private String issuingBank;

    @NotBlank(message = "Advising bank is mandatory")
    private String advisingBank;

    @NotNull(message = "LC amount is mandatory")
    @DecimalMin(value = "0.01", message = "LC amount must be greater than zero")
    private BigDecimal lcAmount;

    @NotNull(message = "Expiry date is mandatory")
    @Future(message = "LC expiry date must be in the future")
    private LocalDate expiryDate;

    @NotNull(message = "Tenor days is mandatory")
    @Min(value = 1, message = "Tenor days must be at least 1")
    private Integer tenorDays;

    private String documentUrl;
}
