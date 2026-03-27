package com.fatihozkurt.fatihozkurtcom.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a nullable string is non-blank when present.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OptionalNotBlankValidator.class)
public @interface OptionalNotBlank {
    String message() default "{validation.required}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
