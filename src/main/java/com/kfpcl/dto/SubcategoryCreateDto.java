package com.kfpcl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubcategoryCreateDto {

    private String id;

    @NotBlank(message = "categoryId is required")
    private String categoryId;

    @NotBlank(message = "Subcategory name is required")
    private String name;

    private String imageUrl;

    private String description;

    private Integer displayOrder;

    private Double discount;

    @JsonProperty("isActive")
    @Builder.Default
    private Boolean isActive = true;

    private String status;
}
