package com.payment.repository;

import com.payment.entity.Refund;
import com.payment.entity.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByOrderId(String orderId);

    Optional<Refund> findByRefundReference(String refundReference);

    @Query("SELECT COALESCE(SUM(r.approvedAmount), 0) FROM Refund r WHERE r.orderId = :orderId AND r.status IN :statuses")
    BigDecimal getTotalRefundedAmount(@Param("orderId") String orderId, @Param("statuses") List<RefundStatus> statuses);

    boolean existsByOrderIdAndStatus(String orderId, RefundStatus status);
}
