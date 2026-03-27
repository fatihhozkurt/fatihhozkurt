package com.fatihozkurt.fatihozkurtcom.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates optional string size constraints.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OptionalSizeValidator.class)
public @interface OptionalSize {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
    String message() default "{validation.required}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
