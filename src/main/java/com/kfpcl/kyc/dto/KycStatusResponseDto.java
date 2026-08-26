package com.kfpcl.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kfpcl.user.entity.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycStatusResponseDto {

    @JsonProperty("isVerified")
    private boolean isVerified;

    private KycStatus kycStatus;
    private String rejectionReason;
}
