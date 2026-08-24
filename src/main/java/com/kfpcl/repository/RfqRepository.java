package com.kfpcl.repository;

import com.kfpcl.entity.Rfq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfqRepository extends JpaRepository<Rfq, String> {
    List<Rfq> findByBuyerIdOrderByCreatedAtDesc(String buyerId);
    Optional<Rfq> findByIdAndBuyerId(String id, String buyerId);
    long countByBuyerIdAndStatusIn(String buyerId, List<Rfq.Status> statuses);
    List<Rfq> findTop5ByBuyerIdOrderByCreatedAtDesc(String buyerId);
}
