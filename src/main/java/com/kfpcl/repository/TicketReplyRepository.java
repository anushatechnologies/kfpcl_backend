package com.kfpcl.repository;

import com.kfpcl.entity.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketReplyRepository extends JpaRepository<TicketReply, String> {

    List<TicketReply> findByTicketIdOrderByCreatedAtAsc(String ticketId);
}
