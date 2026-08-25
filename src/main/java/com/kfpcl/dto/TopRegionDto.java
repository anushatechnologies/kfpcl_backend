package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopRegionDto {

    private String region;
    private long totalOrders;
    private double totalRevenue;
    private double marketSharePercentage;
}
