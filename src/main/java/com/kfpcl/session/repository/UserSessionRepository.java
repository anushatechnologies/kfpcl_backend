package com.kfpcl.session.repository;

import com.kfpcl.session.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionId(String sessionId);

    Optional<UserSession> findBySessionIdAndIsActiveTrue(String sessionId);

    List<UserSession> findByUserIdAndIsActiveTrue(String userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId")
    int deactivateAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.expiresAt < :now AND s.isActive = true")
    int deactivateExpiredSessions(@Param("now") LocalDateTime now);
}
