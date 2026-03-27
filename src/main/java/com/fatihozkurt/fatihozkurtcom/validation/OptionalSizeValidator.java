package com.fatihozkurt.fatihozkurtcom.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for {@link OptionalSize}.
 */
public class OptionalSizeValidator implements ConstraintValidator<OptionalSize, String> {

    private int min;
    private int max;

    /**
     * Initializes validator with annotation parameters.
     *
     * @param constraintAnnotation annotation
     */
    @Override
    public void initialize(OptionalSize constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    /**
     * Validates optional size behavior.
     *
     * @param value value
     * @param context context
     * @return true when null or size in range
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int length = value.length();
        return length >= min && length <= max;
    }
}
