package com.payment.entity;

import com.payment.entity.enums.PaymentGatewayType;
import com.payment.entity.enums.PaymentMethod;
import com.payment.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_pt_order_id", columnList = "orderId"),
        @Index(name = "idx_pt_buyer_id", columnList = "buyerId"),
        @Index(name = "idx_pt_seller_id", columnList = "sellerId"),
        @Index(name = "idx_pt_gateway_order_id", columnList = "gatewayOrderId"),
        @Index(name = "idx_pt_gateway_payment_id", columnList = "gatewayPaymentId"),
        @Index(name = "idx_pt_utr_number", columnList = "utrNumber"),
        @Index(name = "idx_pt_idempotency_key", columnList = "idempotencyKey")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String transactionReference;

    @Column(nullable = false, length = 64)
    private String orderId;

    @Column(nullable = false, length = 64)
    private String buyerId;

    @Column(length = 64)
    private String sellerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentGatewayType gateway;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 8)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(length = 128)
    private String gatewayOrderId;

    @Column(length = 128, unique = true)
    private String gatewayPaymentId;

    @Column(length = 128)
    private String gatewayTransactionId;

    @Column(length = 64, unique = true)
    private String utrNumber;

    @Column(length = 128)
    private String remitterBank;

    private LocalDateTime transferDate;

    @Column(length = 512)
    private String receiptDocumentUrl;

    @Column(length = 512)
    private String failureReason;

    @Column(length = 128, unique = true)
    private String idempotencyKey;

    @Builder.Default
    private Boolean invoiceEmailSent = false;

    private LocalDateTime invoiceEmailSentAt;

    @Column(length = 128)
    private String recipientEmail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.invoiceEmailSent == null) {
            this.invoiceEmailSent = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
