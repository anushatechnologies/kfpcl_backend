package com.kfpcl.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    private String id;
    private String productId;
    private String productName;
    private Double price;
    private String imageUrl;
    private LocalDateTime savedAt;
}
