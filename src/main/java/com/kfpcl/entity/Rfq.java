package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfqs", indexes = {
        @Index(name = "idx_rfq_number", columnList = "rfq_number", unique = true),
        @Index(name = "idx_rfq_buyer", columnList = "buyer_id"),
        @Index(name = "idx_rfq_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rfq {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "rfq_number", nullable = false, unique = true, length = 100)
    private String rfqNumber;

    @Column(name = "buyer_id", nullable = false, length = 64)
    private String buyerId;

    @Column(name = "buyer_name", length = 150)
    private String buyerName;

    @Column(name = "product_id", length = 64)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "title", length = 300)
    private String title;

    @Column(name = "category_id", length = 64)
    private String categoryId;

    @Column(name = "specifications", length = 4000)
    private String specifications;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "target_price")
    private Double targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.OPEN;

    @Column(name = "delivery_location", length = 300)
    private String deliveryLocation;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        OPEN,
        CLOSED,
        EXPIRED,
        CANCELLED
    }
}
