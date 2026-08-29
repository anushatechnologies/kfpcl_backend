package com.payment.repository;

import com.payment.entity.PaymentTransaction;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
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
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByTransactionReference(String transactionReference);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.orderId = :orderId ORDER BY pt.createdAt DESC LIMIT 1")
    Optional<PaymentTransaction> findByOrderId(@Param("orderId") String orderId);

    Optional<PaymentTransaction> findTopByOrderIdOrderByCreatedAtDesc(String orderId);

    Optional<PaymentTransaction> findByGatewayOrderId(String gatewayOrderId);

    Optional<PaymentTransaction> findByGatewayPaymentId(String gatewayPaymentId);

    Optional<PaymentTransaction> findByUtrNumber(String utrNumber);

    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    boolean existsByUtrNumber(String utrNumber);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.buyerId = :buyerId " +
           "AND (:status IS NULL OR pt.status = :status) " +
           "AND (:paymentMethod IS NULL OR pt.paymentMethod = :paymentMethod) " +
           "AND (cast(:startDate as timestamp) IS NULL OR pt.createdAt >= :startDate) " +
           "AND (cast(:endDate as timestamp) IS NULL OR pt.createdAt <= :endDate)")
    Page<PaymentTransaction> findBuyerTransactions(
            @Param("buyerId") String buyerId,
            @Param("status") PaymentStatus status,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
