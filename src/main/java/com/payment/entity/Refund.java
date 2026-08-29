package com.payment.entity;

import com.payment.entity.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refund_order_id", columnList = "orderId"),
        @Index(name = "idx_refund_txn_id", columnList = "transactionId"),
        @Index(name = "idx_refund_ref", columnList = "refundReference", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String orderId;

    private Long transactionId;

    @Column(nullable = false, unique = true, length = 64)
    private String refundReference;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal approvedAmount;

    @Column(nullable = false, length = 512)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RefundStatus status;

    @Column(length = 128)
    private String gatewayRefundId;

    @Column(length = 64)
    private String approvedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
