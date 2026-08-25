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
public class AnalyticsOverviewDto {

    private String period; // day, week, month
    private String fromDate;
    private String toDate;
    private double revenue;
    private long totalOrders;
    private long newUsers;
    private long activeListings;
    private Map<String, Object> breakdown;
}
