package com.kfpcl.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDto {

    private String id;

    @NotBlank(message = "productName is required")
    private String productName;

    @NotBlank(message = "categoryId is required")
    private String categoryId;

    @NotBlank(message = "subcategoryId is required")
    private String subcategoryId;

    private String brand;

    private String description;

    private String imageUrl;
    private String regionOfOrigin;
    private String countryOfOrigin;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "mrp is required")
    @DecimalMin(value = "0.01", message = "MRP must be greater than 0")
    private Double mrp;

    private Double quantity;

    private String unit;

    @Min(value = 0, message = "stockQuantity cannot be negative")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Builder.Default
    private String status = "ACTIVE";

    @NotBlank(message = "sku is required")
    private String sku;

    private String measurementType;

    private List<ProductVariantDto> variants;
}
