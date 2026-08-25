package com.kfpcl.controller;

import com.kfpcl.dto.*;
import com.kfpcl.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getSummary() {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Dashboard summary retrieved successfully"));
    }

    @GetMapping("/sales-overview")
    public ResponseEntity<ApiResponse<SalesOverviewDto>> getSalesOverview() {
        SalesOverviewDto sales = dashboardService.getSalesOverview();
        return ResponseEntity.ok(ApiResponse.success(sales, "Sales overview retrieved successfully"));
    }

    @GetMapping("/order-status-breakdown")
    public ResponseEntity<ApiResponse<OrderStatusBreakdownDto>> getOrderStatusBreakdown() {
        OrderStatusBreakdownDto breakdown = dashboardService.getOrderStatusBreakdown();
        return ResponseEntity.ok(ApiResponse.success(breakdown, "Order status breakdown retrieved successfully"));
    }

    @GetMapping("/top-regions")
    public ResponseEntity<ApiResponse<List<TopRegionDto>>> getTopRegions() {
        List<TopRegionDto> regions = dashboardService.getTopRegions();
        return ResponseEntity.ok(ApiResponse.success(regions, "Top regions retrieved successfully"));
    }

    @GetMapping("/latest-sales")
    public ResponseEntity<ApiResponse<List<LatestSaleDto>>> getLatestSales() {
        List<LatestSaleDto> sales = dashboardService.getLatestSales();
        return ResponseEntity.ok(ApiResponse.success(sales, "Latest sales retrieved successfully"));
    }
}
