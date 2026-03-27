package com.fatihozkurt.fatihozkurtcom.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator for {@link OptionalEmail}.
 */
public class OptionalEmailValidator implements ConstraintValidator<OptionalEmail, String> {

    private static final Pattern EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    /**
     * Validates optional email behavior.
     *
     * @param value value
     * @param context context
     * @return true when null or valid email
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || EMAIL.matcher(value).matches();
    }
}
