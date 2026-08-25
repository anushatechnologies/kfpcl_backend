package com.kfpcl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandCreateDto {

    private String id;

    @NotBlank(message = "Brand name is required")
    private String name;

    private String logoUrl;
    private String description;
    private String website;
    private String status;
}
