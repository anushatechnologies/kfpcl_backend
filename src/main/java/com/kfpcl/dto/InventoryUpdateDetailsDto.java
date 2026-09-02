package com.kfpcl.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateDetailsDto {

    @Min(value = 0, message = "stockQuantity cannot be negative")
    private Integer stockQuantity;

    @JsonAlias("reservedStock")
    @Min(value = 0, message = "reservedQuantity cannot be negative")
    private Integer reservedQuantity;

    @Min(value = 0, message = "reorderLevel cannot be negative")
    private Integer reorderLevel;

    private String warehouseLocation;

    private String sku;

    private String reason;
}
