package com.kfpcl.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service("twilioSmsService")
public class TwilioSmsService implements SmsService {

    @Value("${app.sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.sms.twilio.auth-token:}")
    private String authToken;

    @Value("${app.sms.twilio.from-phone:}")
    private String fromPhone;

    @Override
    public void sendOtp(String phone, String otp) {
        String maskedPhone = maskPhone(phone);
        log.info("Dispatching SMS OTP via Twilio to {}", maskedPhone);
        // Twilio integration logic uses accountSid, authToken, fromPhone from environment/config
        // Actual HTTP/SDK call would execute here using configured credentials
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2);
    }
}
