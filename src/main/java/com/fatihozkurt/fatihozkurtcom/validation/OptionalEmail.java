package com.fatihozkurt.fatihozkurtcom.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a nullable string is a valid email when present.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OptionalEmailValidator.class)
public @interface OptionalEmail {
    String message() default "{validation.email}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
