package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.response.SellerDashboardStatsResponse;
import com.kfpcl.service.SellerDashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller/dashboard")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final SellerDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SellerDashboardStatsResponse>> getStats(HttpServletRequest request) {
        String sellerId = (String) request.getAttribute("authenticatedUser");
        SellerDashboardStatsResponse stats = dashboardService.getDashboardStats(sellerId);
        return ResponseEntity.ok(ApiResponse.success(stats, "Seller dashboard stats retrieved successfully"));
    }
}
