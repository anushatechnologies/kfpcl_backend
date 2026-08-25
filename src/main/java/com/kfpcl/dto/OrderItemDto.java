package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private String id;
    private String productId;
    private String productName;
    private String sku;
    private Double price;
    private Integer quantity;
    private Double totalPrice;
}
