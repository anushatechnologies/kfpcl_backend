package com.kfpcl.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BannerDto {

    private String id;

    @JsonAlias({"name", "heading"})
    private String title;

    @JsonAlias({"description"})
    private String subtitle;

    @JsonAlias({"image", "bannerUrl"})
    private String imageUrl;

    @JsonAlias({"link", "targetUrl", "redirectUrl"})
    private String linkUrl;

    @JsonAlias({"order", "position"})
    private Integer displayOrder;

    private String status;

    @JsonAlias({"active"})
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
