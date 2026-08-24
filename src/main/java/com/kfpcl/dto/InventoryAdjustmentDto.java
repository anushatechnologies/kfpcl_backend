package com.kfpcl.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustmentDto {

    @NotBlank(message = "adjustmentType is required (ADD, SUBTRACT, SET, CORRECTION, DAMAGE, SALE, RETURN)")
    private String adjustmentType;

    @NotNull(message = "quantity is required")
    @Min(value = 0, message = "quantity must be 0 or greater")
    private Integer quantity;

    @NotBlank(message = "reason is required for stock adjustment")
    private String reason;

    private String adjustedBy;
}
