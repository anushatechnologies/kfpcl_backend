package com.kfpcl.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PanValidator implements ConstraintValidator<ValidPan, String> {

    // 10-character standard PAN pattern
    public static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    @Override
    public boolean isValid(String pan, ConstraintValidatorContext context) {
        if (pan == null || pan.trim().isEmpty()) {
            return false;
        }
        return PAN_PATTERN.matcher(pan.trim().toUpperCase()).matches();
    }
}
