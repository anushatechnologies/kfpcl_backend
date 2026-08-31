package com.kfpcl.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    private Integer statusCode;

    private Long createdOrderId;

    private LocalDateTime createdAt;

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String responseBody, Integer statusCode, Long createdOrderId, LocalDateTime createdAt) {
        this.idempotencyKey = idempotencyKey;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.createdOrderId = createdOrderId;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    public Long getCreatedOrderId() { return createdOrderId; }
    public void setCreatedOrderId(Long createdOrderId) { this.createdOrderId = createdOrderId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static IdempotencyRecordBuilder builder() { return new IdempotencyRecordBuilder(); }

    public static class IdempotencyRecordBuilder {
        private String idempotencyKey;
        private String responseBody;
        private Integer statusCode;
        private Long createdOrderId;
        private LocalDateTime createdAt;

        public IdempotencyRecordBuilder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public IdempotencyRecordBuilder responseBody(String responseBody) { this.responseBody = responseBody; return this; }
        public IdempotencyRecordBuilder statusCode(Integer statusCode) { this.statusCode = statusCode; return this; }
        public IdempotencyRecordBuilder createdOrderId(Long createdOrderId) { this.createdOrderId = createdOrderId; return this; }
        public IdempotencyRecordBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public IdempotencyRecord build() {
            return new IdempotencyRecord(idempotencyKey, responseBody, statusCode, createdOrderId, createdAt);
        }
    }
}
