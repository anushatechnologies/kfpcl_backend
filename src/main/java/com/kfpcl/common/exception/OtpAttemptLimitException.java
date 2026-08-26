package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class OtpAttemptLimitException extends BaseException {
    public OtpAttemptLimitException(String message) {
        super(ErrorCode.OTP_ATTEMPT_LIMIT_EXCEEDED, message, HttpStatus.BAD_REQUEST);
    }

    public OtpAttemptLimitException() {
        this("Maximum OTP verification attempts exceeded. Please request a new OTP.");
    }
}
