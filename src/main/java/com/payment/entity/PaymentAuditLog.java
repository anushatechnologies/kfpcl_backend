package com.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_audit_logs", indexes = {
        @Index(name = "idx_audit_txn_id", columnList = "transactionId"),
        @Index(name = "idx_audit_order_id", columnList = "orderId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transactionId;

    @Column(nullable = false, length = 64)
    private String orderId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(length = 32)
    private String previousStatus;

    @Column(length = 32)
    private String newStatus;

    @Column(length = 64)
    private String performedBy;

    @Column(length = 64)
    private String source;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 2048)
    private String metadata;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
