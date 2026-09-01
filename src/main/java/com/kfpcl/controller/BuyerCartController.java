package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.CartDto;
import com.kfpcl.dto.request.CartItemUpsertRequest;
import com.kfpcl.service.BuyerCartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyer/cart")
@RequiredArgsConstructor
public class BuyerCartController {

    private final BuyerCartService buyerCartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(HttpServletRequest request) {
        String buyerId = (String) request.getAttribute("authenticatedUser");
        CartDto response = buyerCartService.getCart(buyerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart retrieved successfully"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CartDto>> upsertCartItem(
            @RequestBody CartItemUpsertRequest requestDto,
            HttpServletRequest request) {
        String buyerId = (String) request.getAttribute("authenticatedUser");
        CartDto response = buyerCartService.upsertCartItem(buyerId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart updated successfully"));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDto>> removeCartItem(
            @PathVariable String itemId,
            HttpServletRequest request) {
        String buyerId = (String) request.getAttribute("authenticatedUser");
        CartDto response = buyerCartService.removeCartItem(buyerId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Item removed from cart successfully"));
    }
}
