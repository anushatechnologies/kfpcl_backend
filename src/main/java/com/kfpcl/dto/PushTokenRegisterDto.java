package com.kfpcl.dto;

import jakarta.validation.constraints.NotBlank;

public class PushTokenRegisterDto {

    @NotBlank(message = "Token is required")
    private String token;

    private String deviceType;

    public PushTokenRegisterDto() {}

    public PushTokenRegisterDto(String token, String deviceType) {
        this.token = token;
        this.deviceType = deviceType;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public static PushTokenRegisterDtoBuilder builder() { return new PushTokenRegisterDtoBuilder(); }

    public static class PushTokenRegisterDtoBuilder {
        private String token;
        private String deviceType;

        public PushTokenRegisterDtoBuilder token(String token) { this.token = token; return this; }
        public PushTokenRegisterDtoBuilder deviceType(String deviceType) { this.deviceType = deviceType; return this; }

        public PushTokenRegisterDto build() {
            return new PushTokenRegisterDto(token, deviceType);
        }
    }
}
