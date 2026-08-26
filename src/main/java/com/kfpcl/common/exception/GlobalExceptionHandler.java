package com.kfpcl.common.exception;

import com.kfpcl.common.response.ErrorCode;
import com.kfpcl.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        log.warn("Business exception occurred: [code={}], message: {}", ex.getCode(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldErrorDetail> details = new ArrayList<>();
        String primaryMessage = "Validation failed for request";

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(ErrorResponse.FieldErrorDetail.builder()
                    .field(error.getField())
                    .message(error.getDefaultMessage())
                    .build());
        }

        if (!details.isEmpty()) {
            primaryMessage = details.get(0).getMessage();
        }

        log.warn("Validation error on request: {}", primaryMessage);
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.VALIDATION_FAILED, primaryMessage, details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_REQUEST, "Malformed JSON request or invalid data format");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_REQUEST, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, "The requested endpoint does not exist");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled server exception: ", ex);
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred. Please try again later."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
