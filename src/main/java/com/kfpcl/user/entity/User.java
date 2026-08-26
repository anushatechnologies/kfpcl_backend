package com.kfpcl.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_phone", columnList = "phone"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_kyc_status", columnList = "kyc_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "phone", length = 15, nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    // Buyer / Common Company Details
    @Column(name = "owner_name", length = 100)
    private String ownerName;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Column(name = "address", length = 500)
    private String address;

    // Seller KYC Details
    @Column(name = "gst_number", length = 15)
    private String gstNumber;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", length = 20)
    private KycStatus kycStatus;

    @Column(name = "kyc_doc_url", length = 500)
    private String kycDocUrl;

    @Column(name = "pan_doc_url", length = 500)
    private String panDocUrl;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by_admin_id", length = 36)
    private String approvedByAdminId;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejected_by_admin_id", length = 36)
    private String rejectedByAdminId;

    // Verification & Account State
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.trim().isEmpty()) {
            this.id = "usr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
