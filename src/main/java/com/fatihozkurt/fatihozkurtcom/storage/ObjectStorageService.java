package com.fatihozkurt.fatihozkurtcom.storage;

/**
 * Defines object storage operations used by backend content flows.
 */
public interface ObjectStorageService {

    /**
     * Ensures that a logical bucket exists.
     *
     * @param bucket logical bucket
     */
    void ensureBucketExists(StorageBucket bucket);

    /**
     * Stores binary content under object key.
     *
     * @param bucket logical bucket
     * @param objectKey object key
     * @param bytes content bytes
     * @param contentType content type
     */
    void putObject(StorageBucket bucket, String objectKey, byte[] bytes, String contentType);

    /**
     * Loads binary content from storage.
     *
     * @param bucket logical bucket
     * @param objectKey object key
     * @return stored asset
     */
    StoredAsset getObject(StorageBucket bucket, String objectKey);

    /**
     * Checks object existence.
     *
     * @param bucket logical bucket
     * @param objectKey object key
     * @return true when object exists
     */
    boolean exists(StorageBucket bucket, String objectKey);
}

