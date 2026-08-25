package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets", indexes = {
        @Index(name = "idx_tickets_number", columnList = "ticket_number", unique = true),
        @Index(name = "idx_tickets_user", columnList = "user_id"),
        @Index(name = "idx_tickets_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 100)
    private String ticketNumber;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "user_name", length = 150)
    private String userName;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.OPEN;

    @Column(name = "assigned_to", length = 64)
    private String assignedTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        OPEN,
        IN_PROGRESS,
        RESOLVED
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
