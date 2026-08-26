package com.kfpcl.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_audit_logs")
public class OrderAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus toStatus;

    private String changedBy;

    private String notes;

    private LocalDateTime timestamp;

    public OrderAuditLog() {}

    public OrderAuditLog(Long id, Long orderId, OrderStatus fromStatus, OrderStatus toStatus, String changedBy, String notes, LocalDateTime timestamp) {
        this.id = id;
        this.orderId = orderId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.notes = notes;
        this.timestamp = timestamp;
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public OrderStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(OrderStatus fromStatus) { this.fromStatus = fromStatus; }

    public OrderStatus getToStatus() { return toStatus; }
    public void setToStatus(OrderStatus toStatus) { this.toStatus = toStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static OrderAuditLogBuilder builder() { return new OrderAuditLogBuilder(); }

    public static class OrderAuditLogBuilder {
        private Long id;
        private Long orderId;
        private OrderStatus fromStatus;
        private OrderStatus toStatus;
        private String changedBy;
        private String notes;
        private LocalDateTime timestamp;

        public OrderAuditLogBuilder id(Long id) { this.id = id; return this; }
        public OrderAuditLogBuilder orderId(Long orderId) { this.orderId = orderId; return this; }
        public OrderAuditLogBuilder fromStatus(OrderStatus fromStatus) { this.fromStatus = fromStatus; return this; }
        public OrderAuditLogBuilder toStatus(OrderStatus toStatus) { this.toStatus = toStatus; return this; }
        public OrderAuditLogBuilder changedBy(String changedBy) { this.changedBy = changedBy; return this; }
        public OrderAuditLogBuilder notes(String notes) { this.notes = notes; return this; }
        public OrderAuditLogBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public OrderAuditLog build() {
            return new OrderAuditLog(id, orderId, fromStatus, toStatus, changedBy, notes, timestamp);
        }
    }
}
