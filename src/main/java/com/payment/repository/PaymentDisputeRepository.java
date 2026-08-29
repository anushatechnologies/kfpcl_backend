package com.payment.repository;

import com.payment.entity.PaymentDispute;
import com.payment.entity.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentDisputeRepository extends JpaRepository<PaymentDispute, Long> {

    List<PaymentDispute> findByOrderId(String orderId);

    Optional<PaymentDispute> findByOrderIdAndStatusIn(String orderId, List<DisputeStatus> statuses);

    boolean existsByOrderIdAndStatusIn(String orderId, List<DisputeStatus> statuses);
}
