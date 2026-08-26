package com.kfpcl.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "push_tokens")
public class PushToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String token;

    private String deviceType;

    private LocalDateTime createdAt;

    public PushToken() {}

    public PushToken(Long id, String userId, String token, String deviceType, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.deviceType = deviceType;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static PushTokenBuilder builder() { return new PushTokenBuilder(); }

    public static class PushTokenBuilder {
        private Long id;
        private String userId;
        private String token;
        private String deviceType;
        private LocalDateTime createdAt;

        public PushTokenBuilder id(Long id) { this.id = id; return this; }
        public PushTokenBuilder userId(String userId) { this.userId = userId; return this; }
        public PushTokenBuilder token(String token) { this.token = token; return this; }
        public PushTokenBuilder deviceType(String deviceType) { this.deviceType = deviceType; return this; }
        public PushTokenBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public PushToken build() {
            return new PushToken(id, userId, token, deviceType, createdAt);
        }
    }
}
