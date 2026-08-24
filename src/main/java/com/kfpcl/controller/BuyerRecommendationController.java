package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.ProductResponse;
import com.kfpcl.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer/recommendations")
@RequiredArgsConstructor
public class BuyerRecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRecommendations() {
        List<ProductResponse> recommendations = recommendationService.getBuyerRecommendations();
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
}
