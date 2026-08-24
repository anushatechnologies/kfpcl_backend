package com.kfpcl.service;

import com.kfpcl.dto.WishlistItemResponse;

import java.util.List;

public interface WishlistService {

    WishlistItemResponse addToWishlist(String productId);

    void removeFromWishlist(String productId);

    List<WishlistItemResponse> getBuyerWishlist();
}
