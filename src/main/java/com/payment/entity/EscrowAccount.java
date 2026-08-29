package com.payment.entity;

import com.payment.entity.enums.EscrowReleaseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "escrow_accounts", indexes = {
        @Index(name = "idx_escrow_order_id", columnList = "orderId", unique = true),
        @Index(name = "idx_escrow_txn_id", columnList = "paymentTransactionId"),
        @Index(name = "idx_escrow_va_num", columnList = "virtualAccountNumber", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String orderId;

    @Column(length = 64)
    private Long paymentTransactionId;

    @Column(nullable = false, length = 64)
    private String virtualAccountNumber;

    @Column(nullable = false, length = 32)
    private String ifscCode;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmountLocked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EscrowReleaseStatus releaseStatus;

    private LocalDateTime lockedAt;
    private LocalDateTime releasedAt;
    private LocalDateTime refundedAt;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
