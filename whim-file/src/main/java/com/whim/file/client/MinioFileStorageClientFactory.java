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
public class MinioFileStorageClientFactory extends BaseFileStorageClientFactory<MinioClient> {

    public MinioFileStorageClientFactory(MinioStorageProperties minioStorageProperties) {
        super(minioStorageProperties.getName(),minioStorageProperties.getUrl(),minioStorageProperties.getAccessKey(),minioStorageProperties.getSecretKey(),minioStorageProperties.getBucket(),minioStorageProperties.getBasePath());
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
