package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cart_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "cart_id", nullable = false, length = 64)
    private String cartId;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "variant_id", length = 64)
    private String variantId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
