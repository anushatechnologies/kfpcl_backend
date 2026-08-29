package com.payment.entity;

import com.payment.entity.enums.InvoiceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoice_order_id", columnList = "orderId"),
        @Index(name = "idx_invoice_num", columnList = "invoiceNumber", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String orderId;

    private Long transactionId;

    @Column(nullable = false, unique = true, length = 64)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvoiceType invoiceType;

    @Column(length = 512)
    private String documentUrl;

    @Column(length = 128)
    private String recipientEmail;

    @Builder.Default
    private Boolean emailSent = false;

    private LocalDateTime emailSentAt;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.generatedAt == null) {
            this.generatedAt = LocalDateTime.now();
        }
        if (this.emailSent == null) {
            this.emailSent = false;
        }
    }
}
