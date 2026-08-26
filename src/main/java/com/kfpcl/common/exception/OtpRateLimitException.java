package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class OtpRateLimitException extends BaseException {
    public OtpRateLimitException(String message) {
        super(ErrorCode.OTP_RATE_LIMIT_EXCEEDED, message, HttpStatus.BAD_REQUEST);
    }

    public OtpRateLimitException() {
        this("Maximum OTP requests exceeded. Please try again later.");
    }
}
