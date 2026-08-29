package com.payment.dto.lc;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LcVerificationRequest {

    @NotNull(message = "Approval decision is mandatory")
    private Boolean approved;

    private String verificationRemarks;
    private String rejectionReason;
}
