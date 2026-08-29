package com.payment.entity;

import com.payment.entity.enums.DisputeStatus;
import com.payment.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_disputes", indexes = {
        @Index(name = "idx_dispute_order_id", columnList = "orderId"),
        @Index(name = "idx_dispute_txn_id", columnList = "transactionId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String orderId;

    private Long transactionId;

    @Column(nullable = false, length = 64)
    private String raisedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole raisedByRole;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DisputeStatus status;

    @Column(length = 1000)
    private String resolution;

    @Column(length = 2048)
    private String evidenceUrls;

    @Column(length = 64)
    private String resolvedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
