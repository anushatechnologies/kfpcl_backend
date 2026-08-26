package com.kfpcl.dto.request;

import com.kfpcl.entity.enums.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Size(max = 220, message = "Slug cannot exceed 220 characters")
    private String slug;

    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than zero")
    private BigDecimal basePrice;

    @NotNull(message = "Minimum order quantity (MOQ) is required")
    @Min(value = 1, message = "MOQ must be at least 1")
    private Integer moq;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotBlank(message = "Unit is required (e.g. KG, MT, QUINTAL, BAG)")
    @Size(max = 50, message = "Unit cannot exceed 50 characters")
    private String unit;

    private ProductStatus status;

    private String primaryImageUrl;

    private List<String> imageUrls;

    private Map<String, Object> specifications;

    @Valid
    private List<ProductPriceTierRequest> priceTiers;

    @Size(max = 255, message = "Tags cannot exceed 255 characters")
    private String tags;
}
