package com.kfpcl.repository;

import com.kfpcl.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, String> {
    Page<Inquiry> findByBuyerId(String buyerId, Pageable pageable);
    Page<Inquiry> findBySellerId(String sellerId, Pageable pageable);
    Page<Inquiry> findByBuyerIdAndStatus(String buyerId, Inquiry.Status status, Pageable pageable);
    Page<Inquiry> findBySellerIdAndStatus(String sellerId, Inquiry.Status status, Pageable pageable);
}
