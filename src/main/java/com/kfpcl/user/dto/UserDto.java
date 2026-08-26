package com.kfpcl.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kfpcl.user.entity.KycStatus;
import com.kfpcl.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private String id;
    private String phone;
    private Role role;
    private String ownerName;
    private String companyName;
    private String email;
    private String businessType;
    private String address;
    private String gstNumber;
    private String panNumber;
    private KycStatus kycStatus;
    private String kycDocUrl;
    private String panDocUrl;
    private String rejectionReason;

    @JsonProperty("isVerified")
    private boolean isVerified;

    @JsonProperty("isActive")
    private boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
