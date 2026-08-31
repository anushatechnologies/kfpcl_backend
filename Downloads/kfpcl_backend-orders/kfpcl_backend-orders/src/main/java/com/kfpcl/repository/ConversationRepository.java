package com.kfpcl.repository;

import com.kfpcl.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE (c.participant1Id = :user1 AND c.participant2Id = :user2) OR (c.participant1Id = :user2 AND c.participant2Id = :user1)")
    Optional<Conversation> findByParticipants(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT c FROM Conversation c WHERE c.participant1Id = :userId OR c.participant2Id = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantId(@Param("userId") String userId);
}
