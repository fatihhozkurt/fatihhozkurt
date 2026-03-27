package com.fatihozkurt.fatihozkurtcom.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validator for {@link OptionalUrl}.
 */
public class OptionalUrlValidator implements ConstraintValidator<OptionalUrl, String> {

    /**
     * Validates optional URL behavior.
     *
     * @param value value
     * @param context context
     * @return true when null or valid URL
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            URI uri = new URI(value);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}
