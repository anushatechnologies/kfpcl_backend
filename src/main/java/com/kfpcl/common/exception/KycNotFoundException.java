package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class KycNotFoundException extends BaseException {
    public KycNotFoundException(String message) {
        super(ErrorCode.KYC_NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }

    public KycNotFoundException() {
        this("Seller KYC details not found.");
    }
}
