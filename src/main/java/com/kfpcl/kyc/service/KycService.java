package com.kfpcl.kyc.service;

import com.kfpcl.kyc.dto.KycResubmitRequestDto;
import com.kfpcl.kyc.dto.KycStatusResponseDto;

public interface KycService {

    KycStatusResponseDto getKycStatus(String sellerId);

    KycStatusResponseDto resubmitKyc(String sellerId, KycResubmitRequestDto request);
}
