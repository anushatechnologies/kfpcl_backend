package com.kfpcl.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("mockSmsService")
public class MockSmsService implements SmsService {

    @Override
    public void sendOtp(String phone, String otp) {
        log.info("\n==============================\n[DEV MOCK SMS OTP]\nPhone: {}\nOTP  : {}\n==============================", phone, otp);
    }
}
