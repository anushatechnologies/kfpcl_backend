package com.kfpcl.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RfqCreateRequest {
    private String title;
    private String categoryId;
    private String productId;
    private Integer quantity;
    private Double targetPrice;
    private String deliveryLocation;
    private String specifications;
    private LocalDateTime requiredDeliveryDate;
}
