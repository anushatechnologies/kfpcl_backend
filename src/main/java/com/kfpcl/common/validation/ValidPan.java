package com.kfpcl.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PanValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPan {
    String message() default "Invalid PAN format. Must be a valid 10-character alphanumeric PAN (e.g. AABCA1234F)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
