package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recently_viewed",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_buyer_product_view", columnNames = {"buyer_id", "product_id"})
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentlyViewed {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @UpdateTimestamp
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
}
