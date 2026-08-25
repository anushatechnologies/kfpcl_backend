package com.kfpcl.repository;

import com.kfpcl.entity.SellerApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, String> {

    Optional<SellerApplication> findFirstByUserIdOrderByCreatedAtDesc(String userId);

    Optional<SellerApplication> findByUserId(String userId);

    Page<SellerApplication> findByStatus(SellerApplication.Status status, Pageable pageable);

    boolean existsByUserId(String userId);

    long countByStatus(SellerApplication.Status status);
}
