package com.payment.repository;

import com.payment.entity.LetterOfCredit;
import com.payment.entity.enums.LcStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LetterOfCreditRepository extends JpaRepository<LetterOfCredit, Long> {

    Optional<LetterOfCredit> findByOrderId(String orderId);

    Optional<LetterOfCredit> findByLcNumber(String lcNumber);

    boolean existsByLcNumber(String lcNumber);

    List<LetterOfCredit> findByStatus(LcStatus status);
}
