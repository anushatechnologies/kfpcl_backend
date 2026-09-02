package com.kfpcl.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCreateDto {

    private String productId;

    @NotBlank(message = "sku is required")
    private String sku;

    @Min(value = 0, message = "stockQuantity cannot be negative")
    private Integer stockQuantity;

    @JsonAlias("reservedStock")
    @Min(value = 0, message = "reservedQuantity cannot be negative")
    private Integer reservedQuantity;

    @Min(value = 0, message = "reorderLevel cannot be negative")
    private Integer reorderLevel;

    private String warehouseLocation;
}
