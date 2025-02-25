package com.whim.file.client;

import com.whim.file.FileStorageProperties;
import io.minio.MinioClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author jince
 * date: 2025/2/23 16:58
 * description:
 */
@Getter
@RequiredArgsConstructor
public class MinioFileStorageClientFactory implements IFileStorageClientFactory<MinioClient> {
    private final String name;
    private final String url;
    private final String accessKey;
    private final String secretKey;
    private final String bucket;
    private volatile MinioClient client;

    public MinioFileStorageClientFactory(FileStorageProperties.MinioStorageProperties minioStorageProperties) {
        name = minioStorageProperties.getName();
        url = minioStorageProperties.getUrl();
        accessKey = minioStorageProperties.getAccessKey();
        secretKey = minioStorageProperties.getSecretKey();
        bucket = minioStorageProperties.getBucket();
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
    public void close() throws Exception {
        if (client != null) {
            client = null;
        }
    }
}
