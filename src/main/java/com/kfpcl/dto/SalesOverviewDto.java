package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOverviewDto {

    private double totalSales;
    private double currentMonthSales;
    private double previousMonthSales;
    private double growthPercentage;
    private Map<String, Double> salesByPeriod;
}
