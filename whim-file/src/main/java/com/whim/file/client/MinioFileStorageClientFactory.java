package com.whim.file.client;

import com.whim.file.config.FileStorageProperties.MinioStorageProperties;
import io.minio.MinioClient;
import lombok.Getter;

/**
 * @author jince
 * date: 2025/2/23 16:58
 * description: minio文件存储客户端工厂
 */
@Getter
public class MinioFileStorageClientFactory implements IFileStorageClientFactory<MinioClient> {
    private final String name;
    private final String url;
    private final String accessKey;
    private final String secretKey;
    private final String bucket;
    private final String basePath;
    private volatile MinioClient client;

    public MinioFileStorageClientFactory(MinioStorageProperties minioStorageProperties) {
        name = minioStorageProperties.getName();
        url = minioStorageProperties.getUrl();
        accessKey = minioStorageProperties.getAccessKey();
        secretKey = minioStorageProperties.getSecretKey();
        bucket = minioStorageProperties.getBucket();
        basePath = minioStorageProperties.getBasePath();
    }

    @Override
    public MinioClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = MinioClient.builder()
                            .endpoint(url)
                            .credentials(accessKey, secretKey)
                            .build();
                }
            }
        }
        return client;
    }

    @Override
    public void close() {
        if (client != null) {
            client = null;
        }
    }
}
