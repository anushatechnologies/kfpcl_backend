package com.kfpcl.repository;

import com.kfpcl.entity.Inquiry;
import com.kfpcl.entity.enums.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Inquiry> findByBuyerIdAndStatus(Long buyerId, InquiryStatus status, Pageable pageable);

    Page<Inquiry> findBySellerId(Long sellerId, Pageable pageable);

    Page<Inquiry> findBySellerIdAndStatus(Long sellerId, InquiryStatus status, Pageable pageable);

    Optional<Inquiry> findByIdAndBuyerId(Long id, Long buyerId);

    Optional<Inquiry> findByIdAndSellerId(Long id, Long sellerId);

    long countBySellerIdAndStatus(Long sellerId, InquiryStatus status);
}
