package com.kfpcl.service;

import com.kfpcl.dto.AnalyticsOverviewDto;

import java.util.Map;

public interface AdminAnalyticsService {

    AnalyticsOverviewDto getOverview(String from, String to, String groupBy);

    Map<String, Object> getSalesAnalytics();

    Map<String, Object> getProductAnalytics();

    Map<String, Object> getUserAnalytics();

    Map<String, Object> getRegionAnalytics();
}
