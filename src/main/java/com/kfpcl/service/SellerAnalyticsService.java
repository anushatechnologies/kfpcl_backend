package com.kfpcl.service;

import com.kfpcl.dto.SellerAnalyticsDto;

public interface SellerAnalyticsService {
    SellerAnalyticsDto getSellerAnalytics(String sellerId);
}
