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
public class CartItemDto {
    private String id;
    private String productId;
    private String productName;
    private String variantId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
