package com.kfpcl.auth.sms;

public interface SmsService {
    void sendOtp(String phone, String otp);
}
