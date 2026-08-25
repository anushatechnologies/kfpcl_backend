package com.kfpcl.repository;

import com.kfpcl.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT c FROM Conversation c WHERE c.participantOneId = :userId OR c.participantTwoId = :userId ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findUserConversations(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE (c.participantOneId = :u1 AND c.participantTwoId = :u2) OR (c.participantOneId = :u2 AND c.participantTwoId = :u1)")
    Optional<Conversation> findBetweenUsers(@Param("u1") String u1, @Param("u2") String u2);
}
