package com.kfpcl.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String participant1Id;

    @Column(nullable = false)
    private String participant2Id;

    private String lastMessage;

    private LocalDateTime updatedAt;

    public Conversation() {}

    public Conversation(Long id, String participant1Id, String participant2Id, String lastMessage, LocalDateTime updatedAt) {
        this.id = id;
        this.participant1Id = participant1Id;
        this.participant2Id = participant2Id;
        this.lastMessage = lastMessage;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getParticipant1Id() { return participant1Id; }
    public void setParticipant1Id(String participant1Id) { this.participant1Id = participant1Id; }

    public String getParticipant2Id() { return participant2Id; }
    public void setParticipant2Id(String participant2Id) { this.participant2Id = participant2Id; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static ConversationBuilder builder() { return new ConversationBuilder(); }

    public static class ConversationBuilder {
        private Long id;
        private String participant1Id;
        private String participant2Id;
        private String lastMessage;
        private LocalDateTime updatedAt;

        public ConversationBuilder id(Long id) { this.id = id; return this; }
        public ConversationBuilder participant1Id(String participant1Id) { this.participant1Id = participant1Id; return this; }
        public ConversationBuilder participant2Id(String participant2Id) { this.participant2Id = participant2Id; return this; }
        public ConversationBuilder lastMessage(String lastMessage) { this.lastMessage = lastMessage; return this; }
        public ConversationBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Conversation build() {
            return new Conversation(id, participant1Id, participant2Id, lastMessage, updatedAt);
        }
    }
}
