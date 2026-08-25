package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandUpdateDto {

    private String name;
    private String logoUrl;
    private String description;
    private String website;
    private String status;
}
