package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException() {
        this("Authentication required. Please authenticate.");
    }
}
