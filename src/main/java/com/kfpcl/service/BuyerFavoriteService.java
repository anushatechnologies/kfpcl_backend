package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.response.FavoriteResponse;

public interface BuyerFavoriteService {
    void toggleFavorite(String buyerId, String productId);
    PageResponseDto<FavoriteResponse> getFavorites(String buyerId, int page, int size);
}
