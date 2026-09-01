package com.kfpcl.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDto {

    private String productName;
    private String categoryId;
    private String subcategoryId;
    private String brand;
    private String description;
    private String imageUrl;
    private String regionOfOrigin;
    private String countryOfOrigin;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @DecimalMin(value = "0.01", message = "MRP must be greater than 0")
    private Double mrp;

    private Double quantity;
    private String unit;
    private String status;
    private String sku;

    private String measurementType;

    private List<ProductVariantDto> variants;
}
