package com.fatihozkurt.fatihozkurtcom.storage;

import com.fatihozkurt.fatihozkurtcom.common.exception.AppException;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * Centralizes validation rules for object keys used in storage APIs.
 */
public final class StorageObjectKeyPolicy {

    private static final Pattern OBJECT_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9._\\-/]{1,320}$");

    private StorageObjectKeyPolicy() {
    }

    /**
     * Validates object key and returns normalized key.
     *
     * @param objectKey raw object key
     * @return normalized object key
     */
    public static String requireValid(String objectKey) {
        String normalized = objectKey == null ? null : objectKey.trim();
        if (!StringUtils.hasText(normalized)
                || normalized.startsWith("/")
                || normalized.contains("..")
                || normalized.contains("\\")
                || !OBJECT_KEY_PATTERN.matcher(normalized).matches()) {
            throw new AppException(ErrorCode.VAL001);
        }
        return normalized;
    }
}

