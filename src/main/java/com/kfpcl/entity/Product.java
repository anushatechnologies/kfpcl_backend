package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_sku", columnList = "sku", unique = true),
        @Index(name = "idx_products_category", columnList = "category_id"),
        @Index(name = "idx_products_subcategory", columnList = "subcategory_id"),
        @Index(name = "idx_products_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "category_id", nullable = false, length = 64)
    private String categoryId;

    @Column(name = "subcategory_id", nullable = false, length = 64)
    private String subcategoryId;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(name = "region_of_origin", length = 150)
    private String regionOfOrigin;

    @Column(name = "country_of_origin", length = 150)
    private String countryOfOrigin;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "mrp", nullable = false)
    private Double mrp;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "min_order_quantity", nullable = false)
    @Builder.Default
    private Integer minOrderQuantity = 1;

    @Column(name = "specifications", length = 4000)
    private String specifications;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_type", nullable = false, length = 20)
    @Builder.Default
    private MeasurementType measurementType = MeasurementType.SOLID;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 30)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "seller_id", length = 64)
    private String sellerId;

    @Column(name = "created_by", length = 64)
    @Builder.Default
    private String createdBy = "ADMIN";

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "discount")
    private Double discount;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        ACTIVE,
        INACTIVE,
        ARCHIVED,
        OUT_OF_STOCK
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(this.status);
    }
}
