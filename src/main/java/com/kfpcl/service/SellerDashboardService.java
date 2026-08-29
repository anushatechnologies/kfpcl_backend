package com.kfpcl.service;

import com.kfpcl.dto.response.SellerDashboardStatsResponse;

public interface SellerDashboardService {
    SellerDashboardStatsResponse getDashboardStats(String sellerId);
}
