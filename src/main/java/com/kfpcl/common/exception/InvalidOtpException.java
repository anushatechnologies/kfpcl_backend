package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidOtpException extends BaseException {
    public InvalidOtpException(String message) {
        super(ErrorCode.INVALID_OTP, message, HttpStatus.BAD_REQUEST);
    }

    public InvalidOtpException() {
        this("Invalid OTP entered. Please check and try again.");
    }
}
