package com.kfpcl.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerDashboardStatsResponse {
    private long totalProducts;
    private long activeProducts;
    private long openInquiries;
    private long submittedQuotes;
    private double totalRevenue;
}
