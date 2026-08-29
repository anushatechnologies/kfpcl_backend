package com.payment.entity;

import com.payment.entity.enums.LcStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "letter_of_credits", indexes = {
        @Index(name = "idx_lc_order_id", columnList = "orderId"),
        @Index(name = "idx_lc_number", columnList = "lcNumber", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LetterOfCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String orderId;

    private Long transactionId;

    @Column(nullable = false, length = 64, unique = true)
    private String lcNumber;

    @Column(nullable = false, length = 128)
    private String issuingBank;

    @Column(nullable = false, length = 128)
    private String advisingBank;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal lcAmount;

    @Column(nullable = false)
    private Integer tenorDays;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 512)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LcStatus status;

    @Column(length = 512)
    private String rejectionReason;

    @Column(length = 64)
    private String verifiedBy;

    @Column(length = 512)
    private String verificationRemarks;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
    }
}
