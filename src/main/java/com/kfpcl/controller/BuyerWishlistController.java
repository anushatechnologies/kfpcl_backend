package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.WishlistItemResponse;
import com.kfpcl.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buyer/wishlist")
@RequiredArgsConstructor
public class BuyerWishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addToWishlist(
            @PathVariable("productId") String productId
    ) {
        WishlistItemResponse response = wishlistService.addToWishlist(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @PathVariable("productId") String productId
    ) {
        wishlistService.removeFromWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getWishlist() {
        List<WishlistItemResponse> wishlist = wishlistService.getBuyerWishlist();
        return ResponseEntity.ok(ApiResponse.success(wishlist));
    }
}
