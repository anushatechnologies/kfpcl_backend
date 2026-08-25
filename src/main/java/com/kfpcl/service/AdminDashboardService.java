package com.kfpcl.service;

import com.kfpcl.dto.*;

import java.util.List;

public interface AdminDashboardService {

    DashboardSummaryDto getDashboardSummary();

    SalesOverviewDto getSalesOverview();

    OrderStatusBreakdownDto getOrderStatusBreakdown();

    List<TopRegionDto> getTopRegions();

    List<LatestSaleDto> getLatestSales();
}
