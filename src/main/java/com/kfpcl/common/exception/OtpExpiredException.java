package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class OtpExpiredException extends BaseException {
    public OtpExpiredException(String message) {
        super(ErrorCode.OTP_EXPIRED, message, HttpStatus.BAD_REQUEST);
    }

    public OtpExpiredException() {
        this("OTP has expired. Please request a new one.");
    }
}
