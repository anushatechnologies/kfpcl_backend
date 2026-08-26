package com.kfpcl.controller;

import com.kfpcl.dto.response.ApiResponse;
import com.kfpcl.dto.response.FavoriteResponse;
import com.kfpcl.dto.response.FavoriteToggleResponse;
import com.kfpcl.dto.response.PageResponse;
import com.kfpcl.service.BuyerFavoriteService;
import com.kfpcl.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/buyer/favorites")
@RequiredArgsConstructor
public class BuyerFavoriteController {

    private final BuyerFavoriteService buyerFavoriteService;

    /**
     * Protected API: Toggle product favorite state (save/remove).
     * POST /api/v1/buyer/favorites/{productId}
     */
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<FavoriteToggleResponse>> toggleFavorite(
            @PathVariable Long productId,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        FavoriteToggleResponse response = buyerFavoriteService.toggleFavorite(email, productId);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    /**
     * Protected API: List saved/favorite products for the authenticated buyer.
     * GET /api/v1/buyer/favorites
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FavoriteResponse>>> getBuyerFavorites(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Principal principal) {
        String email = principal != null ? principal.getName() : SecurityUtil.getCurrentUserEmail();
        PageResponse<FavoriteResponse> favorites = buyerFavoriteService.getBuyerFavorites(email, page, size);
        return ResponseEntity.ok(ApiResponse.success("Buyer favorites retrieved successfully", favorites));
    }
}
