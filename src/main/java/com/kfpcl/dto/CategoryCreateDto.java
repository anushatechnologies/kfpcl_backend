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
public class CategoryCreateDto {

    private String id;

    @NotBlank(message = "Category name is required")
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
