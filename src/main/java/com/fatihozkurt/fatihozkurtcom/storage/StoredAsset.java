package com.fatihozkurt.fatihozkurtcom.storage;

/**
 * Represents a binary asset returned from object storage.
 *
 * @param objectKey object key
 * @param contentType content type
 * @param sizeBytes content size
 * @param bytes content bytes
 */
public record StoredAsset(
        String objectKey,
        String contentType,
        long sizeBytes,
        byte[] bytes
) {
}

