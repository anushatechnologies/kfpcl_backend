package com.kfpcl.repository;

import com.kfpcl.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {
    List<PushToken> findByUserId(String userId);

    Optional<PushToken> findByUserIdAndToken(String userId, String token);
}
