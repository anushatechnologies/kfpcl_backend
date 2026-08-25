package com.kfpcl.repository;

import com.kfpcl.entity.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, String> {

    List<Quotation> findByRfqId(String rfqId);

    Page<Quotation> findByStatus(Quotation.Status status, Pageable pageable);

    Page<Quotation> findBySellerId(String sellerId, Pageable pageable);
}
