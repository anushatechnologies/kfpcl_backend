package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_profiles", indexes = {
        @Index(name = "idx_seller_profile_user", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfile {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true, length = 64)
    private String userId;

    @Column(name = "store_name", nullable = false, length = 200)
    private String storeName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "logo_url", columnDefinition = "LONGTEXT")
    private String logoUrl;

    @Column(name = "banner_url", columnDefinition = "LONGTEXT")
    private String bannerUrl;

    @Column(name = "address", length = 500)
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
