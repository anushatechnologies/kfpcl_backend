package com.kfpcl.common.response;

public class ErrorCode {
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String INVALID_OTP = "INVALID_OTP";
    public static final String OTP_EXPIRED = "OTP_EXPIRED";
    public static final String OTP_ATTEMPT_LIMIT_EXCEEDED = "OTP_ATTEMPT_LIMIT_EXCEEDED";
    public static final String OTP_RATE_LIMIT_EXCEEDED = "OTP_RATE_LIMIT_EXCEEDED";
    public static final String OTP_ALREADY_USED = "OTP_ALREADY_USED";
    
    public static final String SESSION_EXPIRED = "SESSION_EXPIRED";
    public static final String INVALID_SESSION = "INVALID_SESSION";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String DUPLICATE_PHONE = "DUPLICATE_PHONE";
    public static final String DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    
    public static final String INVALID_GSTIN = "INVALID_GSTIN";
    public static final String INVALID_PAN = "INVALID_PAN";
    
    public static final String KYC_NOT_FOUND = "KYC_NOT_FOUND";
    public static final String KYC_ALREADY_APPROVED = "KYC_ALREADY_APPROVED";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
}
