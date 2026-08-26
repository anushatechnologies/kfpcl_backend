package com.kfpcl.entity;

import com.kfpcl.entity.enums.RFQStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(
    name = "rfqs",
    indexes = {
        @Index(name = "idx_rfqs_buyer_id", columnList = "buyer_id"),
        @Index(name = "idx_rfqs_category_id", columnList = "category_id"),
        @Index(name = "idx_rfqs_status", columnList = "status"),
        @Index(name = "idx_rfqs_expected_delivery_date", columnList = "expected_delivery_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rfq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(name = "target_unit_price", precision = 12, scale = 2)
    private BigDecimal targetUnitPrice;

    @Column(name = "delivery_location", nullable = false, length = 255)
    private String deliveryLocation;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "payment_terms", length = 255)
    private String paymentTerms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RFQStatus status = RFQStatus.OPEN;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specifications", columnDefinition = "json")
    @Builder.Default
    private Map<String, Object> specifications = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
