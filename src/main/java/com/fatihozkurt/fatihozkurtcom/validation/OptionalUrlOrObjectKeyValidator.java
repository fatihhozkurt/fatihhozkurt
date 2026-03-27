package com.fatihozkurt.fatihozkurtcom.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * Validator for {@link OptionalUrlOrObjectKey}.
 */
public class OptionalUrlOrObjectKeyValidator implements ConstraintValidator<OptionalUrlOrObjectKey, String> {

    private static final Pattern OBJECT_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9._\\-/]{1,320}$");

    /**
     * Validates optional URL or object-key behavior.
     *
     * @param value value
     * @param context context
     * @return true when null or valid URL/object key
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String trimmed = value.trim();
        if (!StringUtils.hasText(trimmed)) {
            return false;
        }
        if (isAbsoluteUrl(trimmed)) {
            return true;
        }
        return !trimmed.startsWith("/")
                && !trimmed.contains("\\")
                && !trimmed.contains("..")
                && OBJECT_KEY_PATTERN.matcher(trimmed).matches();
    }

    private boolean isAbsoluteUrl(String value) {
        try {
            URI uri = new URI(value);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}

