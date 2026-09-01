package com.kfpcl.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemUpsertRequest {
    private String productId;
    private String variantId;
    private Integer quantity;
}
