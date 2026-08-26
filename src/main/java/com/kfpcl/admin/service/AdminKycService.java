package com.kfpcl.admin.service;

import com.kfpcl.kyc.dto.KycApprovalResponseDto;
import com.kfpcl.kyc.dto.KycRejectionRequestDto;
import com.kfpcl.kyc.dto.KycStatusResponseDto;

public interface AdminKycService {

    KycApprovalResponseDto approveKyc(String supplierId, String adminId);

    KycStatusResponseDto rejectKyc(String supplierId, KycRejectionRequestDto request, String adminId);
}
