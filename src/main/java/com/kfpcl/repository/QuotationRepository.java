package com.kfpcl.repository;

import com.kfpcl.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, String> {
    List<Quotation> findByRfqId(String rfqId);
    List<Quotation> findByRfqIdOrderByQuotedPriceAsc(String rfqId);
    Optional<Quotation> findByIdAndRfqId(String id, String rfqId);
    List<Quotation> findByRfqIdAndStatus(String rfqId, Quotation.Status status);
}
