package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class DuplicatePhoneException extends BaseException {
    public DuplicatePhoneException(String message) {
        super(ErrorCode.DUPLICATE_PHONE, message, HttpStatus.CONFLICT);
    }

    public DuplicatePhoneException() {
        this("Phone number is already registered. Please login instead.");
    }
}
