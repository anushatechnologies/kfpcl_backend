package com.kfpcl.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Primary
@Service
public class DelegatingSmsService implements SmsService {

    private final Map<String, SmsService> smsServices;

    @Value("${app.sms.provider:mock}")
    private String provider;

    public DelegatingSmsService(Map<String, SmsService> smsServices) {
        this.smsServices = smsServices;
    }

    @Override
    public void sendOtp(String phone, String otp) {
        String serviceBeanName = provider.toLowerCase() + "SmsService";
        SmsService service = smsServices.get(serviceBeanName);
        if (service == null) {
            log.warn("Configured SMS provider '{}' not found, falling back to mock provider", provider);
            service = smsServices.get("mockSmsService");
        }
        if (service != null) {
            service.sendOtp(phone, otp);
        } else {
            log.error("No valid SMS service bean available");
        }
    }
}
