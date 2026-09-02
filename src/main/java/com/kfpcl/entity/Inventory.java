package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventories", indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id", unique = true),
        @Index(name = "idx_inventory_sku", columnList = "sku")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "product_id", nullable = false, unique = true, length = 64)
    private String productId;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.IN_STOCK;

    @Column(name = "warehouse_location", length = 200)
    private String warehouseLocation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        IN_STOCK,
        LOW_STOCK,
        OUT_OF_STOCK
    }

    public void recalculateStatus() {
        int available = this.stockQuantity - this.reservedQuantity;
        if (available <= 0) {
            this.status = Status.OUT_OF_STOCK;
        } else if (available <= this.reorderLevel) {
            this.status = Status.LOW_STOCK;
        } else {
            this.status = Status.IN_STOCK;
        }
    }
}
