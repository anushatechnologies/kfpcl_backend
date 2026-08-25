package com.kfpcl.repository;

import com.kfpcl.entity.Rfq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RfqRepository extends JpaRepository<Rfq, String>, JpaSpecificationExecutor<Rfq> {

    Optional<Rfq> findByRfqNumber(String rfqNumber);

    Page<Rfq> findByStatus(Rfq.Status status, Pageable pageable);

    Page<Rfq> findByBuyerId(String buyerId, Pageable pageable);

    long countByStatus(Rfq.Status status);
}
