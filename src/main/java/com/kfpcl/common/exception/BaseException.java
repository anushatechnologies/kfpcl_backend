package com.kfpcl.common.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {
    private final String code;
    private final HttpStatus httpStatus;

    protected BaseException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
