package com.kfpcl.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = GstinValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGstin {
    String message() default "Invalid GSTIN format. Must be a valid 15-character alphanumeric GSTIN (e.g. 27AABCA1234F1Z1)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
