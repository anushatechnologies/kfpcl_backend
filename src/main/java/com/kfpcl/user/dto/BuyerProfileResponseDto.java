package com.kfpcl.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class BuyerProfileResponseDto {
    private String id;
    private String phone;
    private Role role;
    private String ownerName;
    private String companyName;
    private String email;
    private String businessType;
    private String address;

    @JsonProperty("isVerified")
    private boolean isVerified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
