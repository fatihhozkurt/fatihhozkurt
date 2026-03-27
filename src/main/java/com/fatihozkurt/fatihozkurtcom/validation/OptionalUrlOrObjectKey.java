package com.fatihozkurt.fatihozkurtcom.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a nullable string is a URL or an object key when present.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OptionalUrlOrObjectKeyValidator.class)
public @interface OptionalUrlOrObjectKey {
    String message() default "{validation.url_or_object_key}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

