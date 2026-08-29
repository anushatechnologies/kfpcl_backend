package com.payment.entity;

import com.payment.entity.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_payouts", indexes = {
        @Index(name = "idx_payout_seller_id", columnList = "sellerId"),
        @Index(name = "idx_payout_order_id", columnList = "orderId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String sellerId;

    @Column(nullable = false, length = 64)
    private String orderId;

    private Long escrowAccountId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal grossAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal platformFee;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal taxDeduction;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PayoutStatus status;

    @Column(length = 128)
    private String bankReference;

    @Column(nullable = false)
    private LocalDateTime initiatedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (this.initiatedAt == null) {
            this.initiatedAt = LocalDateTime.now();
        }
    }
}
