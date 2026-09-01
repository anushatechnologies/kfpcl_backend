package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_buyer", columnList = "buyer_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "buyer_id", nullable = false, unique = true, length = 64)
    private String buyerId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
