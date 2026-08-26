package com.kfpcl.kyc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kfpcl.user.entity.KycStatus;
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
public class KycApprovalResponseDto {
    private String supplierId;

    @JsonProperty("isVerified")
    private boolean isVerified;

    private KycStatus kycStatus;
    private LocalDateTime approvedAt;
    private String approvedByAdminId;
}
