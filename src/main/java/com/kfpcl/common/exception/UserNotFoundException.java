package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException() {
        this("User account not found.");
    }
}
