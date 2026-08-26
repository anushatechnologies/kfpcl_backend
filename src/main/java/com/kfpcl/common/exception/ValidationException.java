package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message, HttpStatus.BAD_REQUEST);
    }
}
