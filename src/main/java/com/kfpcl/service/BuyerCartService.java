package com.kfpcl.service;

import com.kfpcl.dto.CartDto;
import com.kfpcl.dto.request.CartItemUpsertRequest;

public interface BuyerCartService {
    CartDto getCart(String buyerId);
    CartDto upsertCartItem(String buyerId, CartItemUpsertRequest request);
    CartDto removeCartItem(String buyerId, String itemId);
}
