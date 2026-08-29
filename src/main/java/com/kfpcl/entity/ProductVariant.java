package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_product_variants_sku", columnList = "sku", unique = true),
        @Index(name = "idx_product_variants_product", columnList = "product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "product")
@EqualsAndHashCode(exclude = "product")
public class ProductVariant {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "variant_name", nullable = false, length = 100)
    private String variantName;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "mrp", nullable = false)
    private Double mrp;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
