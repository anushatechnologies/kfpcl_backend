package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.response.FavoriteResponse;
import com.kfpcl.service.BuyerFavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyer/favorites")
@RequiredArgsConstructor
public class BuyerFavoriteController {

    private final BuyerFavoriteService favoriteService;

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> toggleFavorite(
            @PathVariable String productId,
            HttpServletRequest request) {
        
        String buyerId = (String) request.getAttribute("authenticatedUser");
        favoriteService.toggleFavorite(buyerId, productId);
        return ResponseEntity.ok(ApiResponse.success("Success", "Favorite toggled successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<FavoriteResponse>>> listFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        String buyerId = (String) request.getAttribute("authenticatedUser");
        PageResponseDto<FavoriteResponse> favorites = favoriteService.getFavorites(buyerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(favorites, "Favorites retrieved successfully"));
    }
}
