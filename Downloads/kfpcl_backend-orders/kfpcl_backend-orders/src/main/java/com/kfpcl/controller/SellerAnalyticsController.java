package com.kfpcl.controller;

import com.kfpcl.dto.SellerAnalyticsDto;
import com.kfpcl.service.SellerAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seller/analytics")
public class SellerAnalyticsController {

    private final SellerAnalyticsService analyticsService;

    public SellerAnalyticsController(SellerAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<SellerAnalyticsDto> getSellerAnalytics(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "seller_1") String sellerId) {
        SellerAnalyticsDto analytics = analyticsService.getSellerAnalytics(sellerId);
        return ResponseEntity.ok(analytics);
    }
}

