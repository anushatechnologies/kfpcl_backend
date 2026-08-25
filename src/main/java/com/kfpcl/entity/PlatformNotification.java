package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_status", columnList = "status"),
        @Index(name = "idx_notifications_audience", columnList = "audience")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformNotification {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @Column(name = "audience", nullable = false, length = 100)
    private String audience; // ALL, BUYERS, SELLERS, REGION_NORTH, etc.

    @Column(name = "channels", length = 150)
    private String channels; // IN_APP, SMS, EMAIL, PUSH

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.SCHEDULED;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        SCHEDULED,
        DISPATCHED,
        CANCELLED
    }
}
