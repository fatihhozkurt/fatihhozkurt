package com.fatihozkurt.fatihozkurtcom.storage;

import com.fatihozkurt.fatihozkurtcom.config.AppProperties;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures object storage service beans.
 */
@Configuration
public class StorageConfiguration {

    /**
     * Builds MinIO client when storage is enabled.
     *
     * @param appProperties app properties
     * @return minio client
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.storage", name = "enabled", havingValue = "true")
    public MinioClient minioClient(AppProperties appProperties) {
        return MinioClient.builder()
                .endpoint(appProperties.getStorage().getEndpoint())
                .credentials(appProperties.getStorage().getAccessKey(), appProperties.getStorage().getSecretKey())
                .build();
    }

    /**
     * Provides MinIO-backed storage service when enabled.
     *
     * @param minioClient minio client
     * @param appProperties app properties
     * @return storage service
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.storage", name = "enabled", havingValue = "true")
    public ObjectStorageService minioObjectStorageService(MinioClient minioClient, AppProperties appProperties) {
        return new MinioObjectStorageService(minioClient, appProperties);
    }

    /**
     * Provides in-memory storage fallback when MinIO is disabled.
     *
     * @param appProperties app properties
     * @return in-memory storage
     */
    @Bean
    @ConditionalOnMissingBean(ObjectStorageService.class)
    public ObjectStorageService inMemoryObjectStorageService(AppProperties appProperties) {
        return new InMemoryObjectStorageService(appProperties);
    }
}

