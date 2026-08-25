package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ReviewCreateDto;
import com.kfpcl.dto.ReviewResponseDto;
import com.kfpcl.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(
            @PathVariable String productId,
            @Valid @RequestBody ReviewCreateDto dto,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("authenticatedUser");
        ReviewResponseDto created = reviewService.createProductReview(productId, dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Review submitted successfully and is pending moderation"));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<ApiResponse<PageResponseDto<ReviewResponseDto>>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<ReviewResponseDto> reviews = reviewService.getProductReviews(productId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Product reviews retrieved successfully"));
    }
}
