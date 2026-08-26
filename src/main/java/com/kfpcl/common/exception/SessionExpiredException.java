package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class SessionExpiredException extends BaseException {
    public SessionExpiredException(String message) {
        super(ErrorCode.SESSION_EXPIRED, message, HttpStatus.UNAUTHORIZED);
    }

    public SessionExpiredException() {
        this("Your session has expired. Please authenticate again.");
    }
}
