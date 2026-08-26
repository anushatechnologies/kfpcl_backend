package com.kfpcl.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service("fast2SmsService")
public class Fast2SmsService implements SmsService {

    @Value("${app.sms.fast2sms.api-key:}")
    private String apiKey;

    @Override
    public void sendOtp(String phone, String otp) {
        String maskedPhone = maskPhone(phone);
        log.info("Dispatching SMS OTP via Fast2SMS to {}", maskedPhone);
        // Fast2SMS integration logic using apiKey
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2);
    }
}
