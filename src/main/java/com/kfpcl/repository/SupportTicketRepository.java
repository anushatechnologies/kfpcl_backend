package com.kfpcl.repository;

import com.kfpcl.entity.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, String>, JpaSpecificationExecutor<SupportTicket> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    Page<SupportTicket> findByStatus(SupportTicket.Status status, Pageable pageable);

    Page<SupportTicket> findByUserId(String userId, Pageable pageable);

    long countByStatus(SupportTicket.Status status);
}
