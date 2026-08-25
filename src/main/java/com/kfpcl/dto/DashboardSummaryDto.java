package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    private long totalUsers;
    private long totalBuyers;
    private long totalSellers;
    private long totalProducts;
    private long pendingProductApprovals;
    private long totalOrders;
    private double totalRevenue;
    private long lowStockAlerts;
    private long openSupportTickets;
}
