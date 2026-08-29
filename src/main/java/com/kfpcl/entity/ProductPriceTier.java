package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_price_tiers", indexes = {
        @Index(name = "idx_tier_product", columnList = "product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceTier {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity;

    @Column(name = "max_quantity")
    private Integer maxQuantity;

    @Column(name = "price", nullable = false)
    private Double price;
}
