package com.kfpcl.entity;

import com.kfpcl.entity.enums.QuotationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "quotations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_rfq_seller", columnNames = {"rfq_id", "seller_id"})
    },
    indexes = {
        @Index(name = "idx_quotations_rfq_id", columnList = "rfq_id"),
        @Index(name = "idx_quotations_seller_id", columnList = "seller_id"),
        @Index(name = "idx_quotations_status", columnList = "status"),
        @Index(name = "idx_quotations_total_amount", columnList = "total_amount")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "freight_charges", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal freightCharges = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "delivery_timeline_days", nullable = false)
    private Integer deliveryTimelineDays;

    @Column(name = "payment_terms", nullable = false, length = 255)
    private String paymentTerms;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private QuotationStatus status = QuotationStatus.SUBMITTED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculates total quotation amount automatically.
     */
    @PrePersist
    @PreUpdate
    public void calculateTotalAmount() {
        BigDecimal subTotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        BigDecimal freight = this.freightCharges != null ? this.freightCharges : BigDecimal.ZERO;
        BigDecimal tax = this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO;
        this.totalAmount = subTotal.add(freight).add(tax);
    }
}
