package com.kfpcl.controller;

import com.kfpcl.dto.AnalyticsOverviewDto;
import com.kfpcl.dto.ApiResponse;
import com.kfpcl.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AnalyticsOverviewDto>> getOverview(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "day") String groupBy) {

        AnalyticsOverviewDto overview = analyticsService.getOverview(from, to, groupBy);
        return ResponseEntity.ok(ApiResponse.success(overview, "Analytics overview retrieved successfully"));
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesAnalytics() {
        Map<String, Object> sales = analyticsService.getSalesAnalytics();
        return ResponseEntity.ok(ApiResponse.success(sales, "Sales analytics retrieved successfully"));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductAnalytics() {
        Map<String, Object> products = analyticsService.getProductAnalytics();
        return ResponseEntity.ok(ApiResponse.success(products, "Product analytics retrieved successfully"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserAnalytics() {
        Map<String, Object> users = analyticsService.getUserAnalytics();
        return ResponseEntity.ok(ApiResponse.success(users, "User analytics retrieved successfully"));
    }

    @GetMapping("/regions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRegionAnalytics() {
        Map<String, Object> regions = analyticsService.getRegionAnalytics();
        return ResponseEntity.ok(ApiResponse.success(regions, "Region analytics retrieved successfully"));
    }
}
