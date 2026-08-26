package com.kfpcl.service;

import com.kfpcl.dto.response.SellerDashboardStatsResponse;

public interface SellerDashboardService {

    /**
     * Aggregates real-time performance and pipeline statistics for the authenticated seller.
     */
    SellerDashboardStatsResponse getSellerDashboardStats(String sellerEmail);
}
