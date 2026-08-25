package com.kfpcl.controller;

import com.kfpcl.dto.ApiResponse;
import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ReviewResponseDto;
import com.kfpcl.dto.ReviewStatusUpdateDto;
import com.kfpcl.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDto<ReviewResponseDto>>> listReviews(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PageResponseDto<ReviewResponseDto> reviews = reviewService.getAdminReviews(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Admin reviews retrieved successfully"));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> getReview(@PathVariable String reviewId) {
        ReviewResponseDto review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponse.success(review, "Review details retrieved successfully"));
    }

    @PatchMapping("/{reviewId}/status")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> updateReviewStatus(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewStatusUpdateDto dto) {

        ReviewResponseDto updated = reviewService.updateReviewStatus(reviewId, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Review status updated successfully"));
    }
}
