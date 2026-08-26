package com.kfpcl.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private String imageUrl;
    private Long parentId;
    private String parentName;
    private Integer displayOrder;
    private Boolean isActive;
    private List<CategoryResponse> subCategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
