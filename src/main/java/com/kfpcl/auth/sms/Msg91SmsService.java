package com.kfpcl.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service("msg91SmsService")
public class Msg91SmsService implements SmsService {

    @Value("${app.sms.msg91.auth-key:}")
    private String authKey;

    @Value("${app.sms.msg91.template-id:}")
    private String templateId;

    @Override
    public void sendOtp(String phone, String otp) {
        String maskedPhone = maskPhone(phone);
        log.info("Dispatching SMS OTP via MSG91 to {}", maskedPhone);
        // MSG91 integration logic using authKey and templateId
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2);
    }
}
