package com.kfpcl.kyc.dto;

import com.kfpcl.common.validation.ValidGstin;
import com.kfpcl.common.validation.ValidPan;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResubmitRequestDto {

    @NotBlank(message = "GSTIN is required")
    @ValidGstin
    private String gstNumber;

    @NotBlank(message = "PAN is required")
    @ValidPan
    private String panNumber;

    @NotBlank(message = "KYC document URL is required")
    private String kycDocUrl;

    @NotBlank(message = "PAN document URL is required")
    private String panDocUrl;
}
