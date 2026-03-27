package com.fatihozkurt.fatihozkurtcom.api.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Represents metadata based resume replace request.
 *
 * @param fileName file name
 * @param objectKey object key in storage
 * @param contentType content type
 * @param sizeBytes file size
 */
public record ResumeReplaceRequest(
        @NotBlank @Size(max = 180) String fileName,
        @NotBlank @Size(max = 320) String objectKey,
        @NotBlank @Size(max = 120) String contentType,
        @Positive long sizeBytes
) {
}
