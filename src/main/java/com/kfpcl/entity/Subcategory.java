package com.kfpcl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subcategories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subcategory {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 64)
    private String id;

    @Column(name = "category_id", nullable = false, length = 64)
    private String categoryId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "discount")
    private Double discount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        ACTIVE,
        INACTIVE,
        ARCHIVED
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(this.status);
    }
}
