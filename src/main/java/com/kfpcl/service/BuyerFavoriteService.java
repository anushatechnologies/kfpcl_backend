package com.kfpcl.service;

import com.kfpcl.dto.response.FavoriteResponse;
import com.kfpcl.dto.response.FavoriteToggleResponse;
import com.kfpcl.dto.response.PageResponse;

public interface BuyerFavoriteService {

    /**
     * Toggles a product in the buyer's saved favorite list.
     */
    FavoriteToggleResponse toggleFavorite(String userEmail, Long productId);

    /**
     * Retrieves paginated list of saved favorite products for the authenticated buyer.
     */
    PageResponse<FavoriteResponse> getBuyerFavorites(String userEmail, int page, int size);
}
