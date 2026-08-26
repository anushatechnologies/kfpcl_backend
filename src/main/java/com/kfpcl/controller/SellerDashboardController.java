package com.kfpcl.controller;

import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.SellerDashboardStatsResponse;
import com.kfpcl.service.SellerDashboardService;
import com.kfpcl.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/seller/dashboard")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final SellerDashboardService sellerDashboardService;

    /**
     * Protected API: View real-time seller operational, inventory, lead, and bidding statistics.
     * GET /api/v1/seller/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SellerDashboardStatsResponse>> getSellerDashboardStats(Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        SellerDashboardStatsResponse stats = sellerDashboardService.getSellerDashboardStats(email);
        return ResponseEntity.ok(ApiResponse.success("Seller dashboard statistics retrieved successfully", stats));
    }
}
