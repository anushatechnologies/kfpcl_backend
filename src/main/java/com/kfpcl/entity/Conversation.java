package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conv_participant_1", columnList = "participant_one_id"),
        @Index(name = "idx_conv_participant_2", columnList = "participant_two_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "participant_one_id", nullable = false, length = 64)
    private String participantOneId;

    @Column(name = "participant_one_name", length = 150)
    private String participantOneName;

    @Column(name = "participant_two_id", nullable = false, length = 64)
    private String participantTwoId;

    @Column(name = "participant_two_name", length = 150)
    private String participantTwoName;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "last_message", length = 1000)
    private String lastMessage;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
