package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.service.RecentlyViewedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/buyer/products")
@RequiredArgsConstructor
public class RecentlyViewedController {

    private final RecentlyViewedService recentlyViewedService;

    @PostMapping("/{productId}/view")
    public ResponseEntity<ApiResponse<String>> recordProductView(@PathVariable("productId") String productId) {
        recentlyViewedService.recordProductView(productId);
        return ResponseEntity.ok(ApiResponse.success("Product view recorded successfully"));
    }
}
