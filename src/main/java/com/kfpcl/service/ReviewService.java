package com.kfpcl.service;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ReviewCreateDto;
import com.kfpcl.dto.ReviewResponseDto;
import com.kfpcl.dto.ReviewStatusUpdateDto;

public interface ReviewService {

    PageResponseDto<ReviewResponseDto> getAdminReviews(String status, int page, int size, String sortBy, String sortDir);

    ReviewResponseDto getReviewById(String reviewId);

    ReviewResponseDto updateReviewStatus(String reviewId, ReviewStatusUpdateDto dto);

    ReviewResponseDto createProductReview(String productId, ReviewCreateDto dto, String userId);

    PageResponseDto<ReviewResponseDto> getProductReviews(String productId, int page, int size, String sortBy, String sortDir);
}
