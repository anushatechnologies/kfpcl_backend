package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications", indexes = {
        @Index(name = "idx_user_notif_user", columnList = "user_id"),
        @Index(name = "idx_user_notif_read", columnList = "is_read")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false, length = 2000)
    private String body;

    @Column(name = "target_path", length = 500)
    private String targetPath;

    @Column(name = "is_read")
    @Builder.Default
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
