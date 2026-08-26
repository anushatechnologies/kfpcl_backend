package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidSessionException extends BaseException {
    public InvalidSessionException(String message) {
        super(ErrorCode.INVALID_SESSION, message, HttpStatus.UNAUTHORIZED);
    }

    public InvalidSessionException() {
        this("Invalid session. Please authenticate.");
    }
}
