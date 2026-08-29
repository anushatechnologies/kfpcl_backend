package com.payment.repository;

import com.payment.entity.EscrowAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, Long> {

    Optional<EscrowAccount> findByOrderId(String orderId);

    Optional<EscrowAccount> findByVirtualAccountNumber(String virtualAccountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EscrowAccount e WHERE e.orderId = :orderId")
    Optional<EscrowAccount> findByOrderIdForUpdate(@Param("orderId") String orderId);
}
