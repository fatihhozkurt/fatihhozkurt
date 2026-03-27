package com.fatihozkurt.fatihozkurtcom.storage;

import com.fatihozkurt.fatihozkurtcom.common.exception.AppException;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.util.StringUtils;

/**
 * Provides an in-memory object storage fallback for tests and local runs.
 */
public class InMemoryObjectStorageService implements ObjectStorageService {

    private final Map<StorageBucket, Map<String, StoredAsset>> buckets = new EnumMap<>(StorageBucket.class);
    private final AppProperties appProperties;

    /**
     * Creates in-memory storage service.
     *
     * @param appProperties app properties
     */
    public InMemoryObjectStorageService(AppProperties appProperties) {
        this.appProperties = appProperties;
        for (StorageBucket bucket : StorageBucket.values()) {
            buckets.put(bucket, new ConcurrentHashMap<>());
        }
    }

    /**
     * Ensures logical bucket existence.
     *
     * @param bucket logical bucket
     */
    @Override
    public void ensureBucketExists(StorageBucket bucket) {
        buckets.computeIfAbsent(bucket, ignored -> new ConcurrentHashMap<>());
    }

    /**
     * Stores binary content in-memory.
     *
     * @param bucket logical bucket
     * @param objectKey object key
     * @param bytes content bytes
     * @param contentType content type
     */
    @Override
    public void putObject(StorageBucket bucket, String objectKey, byte[] bytes, String contentType) {
        String safeKey = StorageObjectKeyPolicy.requireValid(objectKey);
        String resolvedContentType = StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
        buckets.get(bucket).put(safeKey, new StoredAsset(safeKey, resolvedContentType, bytes.length, bytes));
    }

    /**
     * Loads binary content from in-memory storage.
     *
     * @param bucket logical bucket
     * @param objectKey object key
     * @return stored asset
     */
    @Override
    public StoredAsset getObject(StorageBucket bucket, String objectKey) {
        String safeKey = StorageObjectKeyPolicy.requireValid(objectKey);
        StoredAsset storedAsset = buckets.get(bucket).get(safeKey);
        if (storedAsset == null) {
            throw new AppException(ErrorCode.USR001);
        }
        return storedAsset;
    }

    /**
     * Checks in-memory object existence.
     *
     * @param bucket logical bucket
     * @param objectKey object key
     * @return true when object exists
     */
    @Override
    public boolean exists(StorageBucket bucket, String objectKey) {
        String safeKey = StorageObjectKeyPolicy.requireValid(objectKey);
        return buckets.get(bucket).containsKey(safeKey);
    }

    /**
     * Returns configured bucket name for diagnostics.
     *
     * @param bucket bucket
     * @return configured bucket name
     */
    public String configuredBucketName(StorageBucket bucket) {
        AppProperties.Buckets configuredBuckets = appProperties.getStorage().getBuckets();
        return switch (bucket) {
            case PUBLIC -> configuredBuckets.getPublicAssets();
            case PROJECT -> configuredBuckets.getProjectAssets();
            case RESUME -> configuredBuckets.getResumeAssets();
        };
    }
}

