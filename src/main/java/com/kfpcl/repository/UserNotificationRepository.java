package com.kfpcl.repository;

import com.kfpcl.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {
    Page<UserNotification> findByUserId(String userId, Pageable pageable);
}
