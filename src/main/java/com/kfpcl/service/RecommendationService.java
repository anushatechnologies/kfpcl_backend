package com.kfpcl.service;

import com.kfpcl.dto.ProductResponse;

import java.util.List;

public interface RecommendationService {

    List<ProductResponse> getBuyerRecommendations();
}
