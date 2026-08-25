package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_replies", indexes = {
        @Index(name = "idx_replies_ticket", columnList = "ticket_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReply {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "ticket_id", nullable = false, length = 64)
    private String ticketId;

    @Column(name = "sender_id", nullable = false, length = 64)
    private String senderId;

    @Column(name = "sender_name", length = 150)
    private String senderName;

    @Column(name = "sender_role", length = 50)
    private String senderRole;

    @Column(name = "message", nullable = false, length = 3000)
    private String message;

    @Column(name = "attachments", length = 1000)
    private String attachments;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
