package com.kfpcl.repository;

import com.kfpcl.entity.PlatformNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformNotificationRepository extends JpaRepository<PlatformNotification, String> {

    Page<PlatformNotification> findByStatus(PlatformNotification.Status status, Pageable pageable);

    Page<PlatformNotification> findByAudience(String audience, Pageable pageable);
}
