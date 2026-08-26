package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidPanException extends BaseException {
    public InvalidPanException(String message) {
        super(ErrorCode.INVALID_PAN, message, HttpStatus.BAD_REQUEST);
    }

    public InvalidPanException() {
        this("Invalid PAN format. Please provide a valid 10-character PAN.");
    }
}
