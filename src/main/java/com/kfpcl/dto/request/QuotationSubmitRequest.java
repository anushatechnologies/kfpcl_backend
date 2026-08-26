package com.kfpcl.dto.request;

import jakarta.validation.constraints.*;
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
public class QuotationSubmitRequest {

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than zero")
    private BigDecimal unitPrice;

    @NotNull(message = "Quoted quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @DecimalMin(value = "0.00", message = "Freight charges cannot be negative")
    @Builder.Default
    private BigDecimal freightCharges = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull(message = "Delivery timeline (in days) is required")
    @Min(value = 1, message = "Delivery timeline must be at least 1 day")
    private Integer deliveryTimelineDays;

    @NotBlank(message = "Payment terms are required (e.g., 30% Advance, 70% against LR)")
    @Size(max = 255, message = "Payment terms cannot exceed 255 characters")
    private String paymentTerms;

    @NotNull(message = "Quote validity expiration date is required")
    @Future(message = "Quote validity date must be a future date")
    private LocalDate validUntil;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;
}
