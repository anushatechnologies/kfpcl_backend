package com.kfpcl.serviceImpl;

import com.kfpcl.dto.PageResponseDto;
import com.kfpcl.dto.ReviewCreateDto;
import com.kfpcl.dto.ReviewResponseDto;
import com.kfpcl.dto.ReviewStatusUpdateDto;
import com.kfpcl.entity.Product;
import com.kfpcl.entity.Review;
import com.kfpcl.entity.User;
import com.kfpcl.exception.BusinessValidationException;
import com.kfpcl.exception.DuplicateResourceException;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.ProductRepository;
import com.kfpcl.repository.ReviewRepository;
import com.kfpcl.repository.UserRepository;
import com.kfpcl.service.AuditLogService;
import com.kfpcl.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ReviewResponseDto> getAdminReviews(String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviewPage;
        if (StringUtils.hasText(status)) {
            try {
                Review.Status rStatus = Review.Status.valueOf(status.trim().toUpperCase());
                reviewPage = reviewRepository.findByStatus(rStatus, pageable);
            } catch (IllegalArgumentException e) {
                reviewPage = reviewRepository.findAll(pageable);
            }
        } else {
            reviewPage = reviewRepository.findAll(pageable);
        }

        List<ReviewResponseDto> dtoList = reviewPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(reviewPage, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "reviewId", reviewId));
        return mapToDto(review);
    }

    @Override
    public ReviewResponseDto updateReviewStatus(String reviewId, ReviewStatusUpdateDto dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "reviewId", reviewId));

        Review.Status newStatus;
        try {
            newStatus = Review.Status.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessValidationException("Invalid review status: " + dto.getStatus());
        }

        String oldStatus = review.getStatus().name();
        review.setStatus(newStatus);
        Review saved = reviewRepository.save(review);

        auditLogService.logAction("admin", "ROLE_ADMIN", "UPDATE_REVIEW_STATUS", "REVIEW", reviewId, oldStatus, newStatus.name(), null, null);

        return mapToDto(saved);
    }

    @Override
    public ReviewResponseDto createProductReview(String productId, ReviewCreateDto dto, String userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        String actualUserId = StringUtils.hasText(userId) ? userId : "user_buyer_default";
        if (reviewRepository.existsByProductIdAndUserId(productId, actualUserId)) {
            throw new DuplicateResourceException("Review", "user", "You have already reviewed this product");
        }

        User user = userRepository.findById(actualUserId).orElse(null);

        Review review = Review.builder()
                .id("rev_" + UUID.randomUUID().toString().substring(0, 8))
                .productId(productId)
                .productName(product.getProductName())
                .userId(actualUserId)
                .userName(user != null ? user.getName() : "Verified Buyer")
                .rating(dto.getRating())
                .comment(dto.getComment())
                .status(Review.Status.PENDING) // Pending admin moderation
                .build();

        Review saved = reviewRepository.save(review);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ReviewResponseDto> getProductReviews(String productId, int page, int size, String sortBy, String sortDir) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviewPage = reviewRepository.findByProductIdAndStatus(productId, Review.Status.APPROVED, pageable);
        List<ReviewResponseDto> dtoList = reviewPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponseDto.from(reviewPage, dtoList);
    }

    private ReviewResponseDto mapToDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .productName(review.getProductName())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus().name())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
