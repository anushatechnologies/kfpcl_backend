package com.kfpcl.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfqCreateRequest {

    @NotBlank(message = "RFQ Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Detailed requirement description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;

    @NotNull(message = "Required purchase quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Unit is required (e.g. MT, KG, QUINTAL, BAGS)")
    @Size(max = 50, message = "Unit cannot exceed 50 characters")
    private String unit;

    @DecimalMin(value = "0.01", message = "Target unit price must be greater than zero")
    private BigDecimal targetUnitPrice;

    @NotBlank(message = "Delivery location (City / State / Pincode) is required")
    @Size(max = 255, message = "Delivery location cannot exceed 255 characters")
    private String deliveryLocation;

    @FutureOrPresent(message = "Expected delivery date must be today or in the future")
    private LocalDate expectedDeliveryDate;

    @Size(max = 255, message = "Payment terms cannot exceed 255 characters")
    private String paymentTerms;

    private Map<String, Object> specifications;
}
