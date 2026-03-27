package com.fatihozkurt.fatihozkurtcom.api.dto.publicapi;

/**
 * Represents current resume metadata.
 *
 * @param fileName file name
 * @param contentType mime type
 * @param sizeBytes size
 * @param downloadUrl signed or public download url
 */
public record ResumeResponse(
        String fileName,
        String contentType,
        long sizeBytes,
        String downloadUrl
) {
}
