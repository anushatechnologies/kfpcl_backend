package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class KycAlreadyApprovedException extends BaseException {
    public KycAlreadyApprovedException(String message) {
        super(ErrorCode.KYC_ALREADY_APPROVED, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public KycAlreadyApprovedException() {
        this("Seller KYC is already approved and verified.");
    }
}
