package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponseDto {

    private String id;
    private String name;
    private String slug;
    private String logoUrl;
    private String description;
    private String website;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
