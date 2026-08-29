package com.kfpcl.dto;

import jakarta.validation.constraints.DecimalMin;
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
public class ProductVariantDto {

    private String id;

    @NotBlank(message = "variantName is required")
    private String variantName;

    @NotBlank(message = "sku is required")
    private String sku;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double mrp; // maps to Price in the UI

    private Double price; // maps to Discount Price in the UI (optional)

    @NotNull(message = "stock is required")
    @Min(value = 0, message = "stockQuantity cannot be negative")
    private Integer stockQuantity;

    private Integer displayOrder;

    @Builder.Default
    private Boolean active = true;
}
