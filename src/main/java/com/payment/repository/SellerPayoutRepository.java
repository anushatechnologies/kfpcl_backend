package com.payment.repository;

import com.payment.entity.SellerPayout;
import com.payment.entity.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SellerPayoutRepository extends JpaRepository<SellerPayout, Long> {

    Optional<SellerPayout> findByOrderId(String orderId);

    List<SellerPayout> findBySellerId(String sellerId);

    @Query("SELECT sp FROM SellerPayout sp WHERE sp.sellerId = :sellerId " +
           "AND (:status IS NULL OR sp.status = :status) " +
           "AND (cast(:startDate as timestamp) IS NULL OR sp.initiatedAt >= :startDate) " +
           "AND (cast(:endDate as timestamp) IS NULL OR sp.initiatedAt <= :endDate)")
    Page<SellerPayout> findSellerPayouts(
            @Param("sellerId") String sellerId,
            @Param("status") PayoutStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
