package com.kfpcl.service;

import com.kfpcl.dto.ProductResponse;

import java.util.List;

public interface RecentlyViewedService {

    void recordProductView(String productId);

    List<ProductResponse> getRecentlyViewed();
}
