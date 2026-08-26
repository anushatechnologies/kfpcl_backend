package com.kfpcl.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class GstinValidator implements ConstraintValidator<ValidGstin, String> {

    // 15-character standard GSTIN pattern
    public static final Pattern GSTIN_PATTERN = Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$");

    @Override
    public boolean isValid(String gstin, ConstraintValidatorContext context) {
        if (gstin == null || gstin.trim().isEmpty()) {
            return false;
        }
        return GSTIN_PATTERN.matcher(gstin.trim().toUpperCase()).matches();
    }
}
