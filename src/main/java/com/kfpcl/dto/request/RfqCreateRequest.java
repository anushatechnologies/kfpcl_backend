package com.kfpcl.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfqCreateRequest {

    @JsonAlias({"requirementTitle", "name", "productName"})
    private String title;

    @JsonAlias({"category", "subcategoryId"})
    private String categoryId;

    private String productId;

    private Integer quantity;

    private String unit;

    @JsonAlias({"price", "targetRate"})
    private Double targetPrice;

    @JsonAlias({"location", "destination"})
    private String deliveryLocation;

    @JsonAlias({"description", "details", "notes"})
    private String specifications;

    @JsonAlias({"requiredByDate", "deadline", "deliveryDate"})
    @JsonFormat(pattern = "yyyy-MM-dd[ HH:mm:ss][T][.SSSX]")
    private LocalDateTime requiredDeliveryDate;
}
