package com.kfpcl.dto;

import com.kfpcl.entity.OrderStatus;

import java.time.LocalDateTime;

public class OrderAuditLogDto {
    private Long id;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private String changedBy;
    private String notes;
    private LocalDateTime timestamp;

    public OrderAuditLogDto() {}

    public OrderAuditLogDto(Long id, OrderStatus fromStatus, OrderStatus toStatus, String changedBy, String notes, LocalDateTime timestamp) {
        this.id = id;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.notes = notes;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public static OrderAuditLogDtoBuilder builder() { return new OrderAuditLogDtoBuilder(); }

    public static class OrderAuditLogDtoBuilder {
        private Long id;
        private OrderStatus fromStatus;
        private OrderStatus toStatus;
        private String changedBy;
        private String notes;
        private LocalDateTime timestamp;

        public OrderAuditLogDtoBuilder id(Long id) { this.id = id; return this; }
        public OrderAuditLogDtoBuilder fromStatus(OrderStatus fromStatus) { this.fromStatus = fromStatus; return this; }
        public OrderAuditLogDtoBuilder toStatus(OrderStatus toStatus) { this.toStatus = toStatus; return this; }
        public OrderAuditLogDtoBuilder changedBy(String changedBy) { this.changedBy = changedBy; return this; }
        public OrderAuditLogDtoBuilder notes(String notes) { this.notes = notes; return this; }
        public OrderAuditLogDtoBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public OrderAuditLogDto build() {
            return new OrderAuditLogDto(id, fromStatus, toStatus, changedBy, notes, timestamp);
        }
    }
}
