package com.kfpcl.dto;

import java.time.LocalDateTime;

public class ChatMessageDto {
    private Long id;
    private String conversationId;
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessageDto() {}

    public ChatMessageDto(Long id, String conversationId, String senderId, String receiverId, String content, LocalDateTime timestamp) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static ChatMessageDtoBuilder builder() { return new ChatMessageDtoBuilder(); }

    public static class ChatMessageDtoBuilder {
        private Long id;
        private String conversationId;
        private String senderId;
        private String receiverId;
        private String content;
        private LocalDateTime timestamp;

        public ChatMessageDtoBuilder id(Long id) { this.id = id; return this; }
        public ChatMessageDtoBuilder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public ChatMessageDtoBuilder senderId(String senderId) { this.senderId = senderId; return this; }
        public ChatMessageDtoBuilder receiverId(String receiverId) { this.receiverId = receiverId; return this; }
        public ChatMessageDtoBuilder content(String content) { this.content = content; return this; }
        public ChatMessageDtoBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public ChatMessageDto build() {
            return new ChatMessageDto(id, conversationId, senderId, receiverId, content, timestamp);
        }
    }
}
