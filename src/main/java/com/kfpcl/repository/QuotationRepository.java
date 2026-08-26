package com.kfpcl.repository;

import com.kfpcl.entity.Quotation;
import com.kfpcl.entity.enums.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByRfqIdAndSellerId(Long rfqId, Long sellerId);

    boolean existsByRfqIdAndSellerId(Long rfqId, Long sellerId);

    List<Quotation> findByRfqId(Long rfqId);

    List<Quotation> findByRfqIdOrderByTotalAmountAsc(Long rfqId);

    List<Quotation> findByRfqIdAndIdNot(Long rfqId, Long quoteId);

    List<Quotation> findByRfqIdAndIdNotAndStatus(Long rfqId, Long quoteId, QuotationStatus status);

    Page<Quotation> findBySellerId(Long sellerId, Pageable pageable);

    Page<Quotation> findBySellerIdAndStatus(Long sellerId, QuotationStatus status, Pageable pageable);

    Optional<Quotation> findByIdAndSellerId(Long id, Long sellerId);

    long countByRfqId(Long rfqId);

    long countBySellerId(Long sellerId);

    long countBySellerIdAndStatus(Long sellerId, QuotationStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(q.totalAmount), 0) FROM Quotation q WHERE q.seller.id = :sellerId AND q.status = com.kfpcl.entity.enums.QuotationStatus.ACCEPTED")
    java.math.BigDecimal sumAwardedRevenueBySellerId(@org.springframework.data.repository.query.Param("sellerId") Long sellerId);
}
