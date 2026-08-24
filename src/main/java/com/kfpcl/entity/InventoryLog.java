package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_logs", indexes = {
        @Index(name = "idx_inv_log_inventory", columnList = "inventory_id"),
        @Index(name = "idx_inv_log_product", columnList = "product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLog {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "inventory_id", nullable = false, length = 64)
    private String inventoryId;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 30)
    private AdjustmentType adjustmentType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "adjusted_by", length = 100)
    private String adjustedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum AdjustmentType {
        INITIAL,
        ADD,
        SUBTRACT,
        SET,
        CORRECTION,
        DAMAGE,
        SALE,
        RETURN
    }
}
