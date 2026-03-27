package com.fatihozkurt.fatihozkurtcom.storage;

import com.fatihozkurt.fatihozkurtcom.common.exception.AppException;
import com.fatihozkurt.fatihozkurtcom.common.exception.ErrorCode;
import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Implements object storage operations with MinIO.
 */
@Slf4j
@RequiredArgsConstructor
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final AppProperties appProperties;

    /**
     * Ensures that a bucket exists in MinIO.
     *
     * @param bucket logical bucket
     */
    @Override
    public void ensureBucketExists(StorageBucket bucket) {
        String bucketName = resolveBucketName(bucket);
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket name={}", bucketName);
            }
        } catch (Exception ex) {
            throw storageFailure("ensure bucket", bucketName, ex);
        }
    }

    /**
     * Stores binary content in MinIO.
     *
     * @param bucket logical bucket
     * @param objectKey object key
     * @param bytes content bytes
     * @param contentType content type
     */
    @Override
    public void putObject(StorageBucket bucket, String objectKey, byte[] bytes, String contentType) {
        String bucketName = resolveBucketName(bucket);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(StorageObjectKeyPolicy.requireValid(objectKey))
                            .contentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream")
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .build()
            );
        } catch (Exception ex) {
            throw storageFailure("put object", bucketName + "/" + objectKey, ex);
        }
    }

    /**
     * Loads binary content from MinIO.
     *
     * @param bucket logical bucket
     * @param objectKey object key
     * @return stored asset
     */
    @Override
    public StoredAsset getObject(StorageBucket bucket, String objectKey) {
        String bucketName = resolveBucketName(bucket);
        String safeKey = StorageObjectKeyPolicy.requireValid(objectKey);
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(safeKey).build()
        )) {
            byte[] bytes = inputStream.readAllBytes();
            String contentType = minioClient.statObject(
                            StatObjectArgs.builder().bucket(bucketName).object(safeKey).build()
                    )
                    .contentType();
            return new StoredAsset(safeKey, contentType, bytes.length, bytes);
        } catch (ErrorResponseException ex) {
            if (isNotFound(ex)) {
                throw new AppException(ErrorCode.USR001);
            }
            throw storageFailure("get object", bucketName + "/" + safeKey, ex);
        } catch (Exception ex) {
            throw storageFailure("get object", bucketName + "/" + safeKey, ex);
        }
    }

    /**
     * Checks object existence in MinIO.
     *
     * @param bucket logical bucket
     * @param objectKey object key
     * @return true when object exists
     */
    @Override
    public boolean exists(StorageBucket bucket, String objectKey) {
        String bucketName = resolveBucketName(bucket);
        String safeKey = StorageObjectKeyPolicy.requireValid(objectKey);
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(safeKey).build());
            return true;
        } catch (ErrorResponseException ex) {
            if (isNotFound(ex)) {
                return false;
            }
            throw storageFailure("check object", bucketName + "/" + safeKey, ex);
        } catch (Exception ex) {
            throw storageFailure("check object", bucketName + "/" + safeKey, ex);
        }
    }

    private boolean isNotFound(ErrorResponseException ex) {
        String code = ex.errorResponse() != null ? ex.errorResponse().code() : "";
        return "NoSuchKey".equalsIgnoreCase(code)
                || "NoSuchBucket".equalsIgnoreCase(code)
                || "NotFound".equalsIgnoreCase(code);
    }

    private String resolveBucketName(StorageBucket bucket) {
        AppProperties.Buckets buckets = appProperties.getStorage().getBuckets();
        return switch (bucket) {
            case PUBLIC -> buckets.getPublicAssets();
            case PROJECT -> buckets.getProjectAssets();
            case RESUME -> buckets.getResumeAssets();
        };
    }

    private AppException storageFailure(String operation, String target, Exception ex) {
        log.error("Storage operation failed operation={} target={} message={}", operation, target, ex.getMessage(), ex);
        return new AppException(ErrorCode.SYS002);
    }
}

