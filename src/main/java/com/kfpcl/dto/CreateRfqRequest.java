package com.kfpcl.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRfqRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    private String productTitle;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    @Positive(message = "Target price must be greater than zero")
    private BigDecimal targetPrice;

    @NotNull(message = "Expected delivery date is required")
    @Future(message = "Expected delivery date must be in the future")
    private LocalDate expectedDeliveryDate;

    private String description;

    private List<String> attachments;
}
