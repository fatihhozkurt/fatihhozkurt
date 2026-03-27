package com.fatihozkurt.fatihozkurtcom.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for {@link OptionalNotBlank}.
 */
public class OptionalNotBlankValidator implements ConstraintValidator<OptionalNotBlank, String> {

    /**
     * Validates optional non-blank behavior.
     *
     * @param value value
     * @param context context
     * @return true when null or non-blank
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || !value.trim().isEmpty();
    }
}
