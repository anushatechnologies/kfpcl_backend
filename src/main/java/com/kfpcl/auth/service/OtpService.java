package com.kfpcl.auth.service;

import com.kfpcl.auth.entity.OtpPurpose;

public interface OtpService {

    void generateAndSendOtp(String phone, OtpPurpose purpose);

    void verifyOtp(String phone, String rawOtp, OtpPurpose purpose);
}
