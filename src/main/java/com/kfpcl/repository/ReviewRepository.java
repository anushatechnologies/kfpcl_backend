package com.kfpcl.repository;

import com.kfpcl.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {

    Page<Review> findByProductIdAndStatus(String productId, Review.Status status, Pageable pageable);

    List<Review> findByProductId(String productId);

    Page<Review> findByStatus(Review.Status status, Pageable pageable);

    boolean existsByProductIdAndUserId(String productId, String userId);
}
