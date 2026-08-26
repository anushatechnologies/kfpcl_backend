package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidGstinException extends BaseException {
    public InvalidGstinException(String message) {
        super(ErrorCode.INVALID_GSTIN, message, HttpStatus.BAD_REQUEST);
    }

    public InvalidGstinException() {
        this("Invalid GSTIN format. Please provide a valid 15-character GSTIN.");
    }
}
