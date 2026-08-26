package com.kfpcl.repository;

import com.kfpcl.entity.Rfq;
import com.kfpcl.entity.enums.RFQStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RfqRepository extends JpaRepository<Rfq, Long>, JpaSpecificationExecutor<Rfq> {

    Page<Rfq> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Rfq> findByBuyerIdAndStatus(Long buyerId, RFQStatus status, Pageable pageable);

    Optional<Rfq> findByIdAndBuyerId(Long id, Long buyerId);

    Page<Rfq> findByStatus(RFQStatus status, Pageable pageable);

    Page<Rfq> findByStatusAndCategoryId(RFQStatus status, Long categoryId, Pageable pageable);

    long countByBuyerIdAndStatus(Long buyerId, RFQStatus status);
}
