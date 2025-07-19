package com.whim.file.client;

import com.whim.file.config.properties.FileStorageProperties.MinioStorageProperties;
import io.minio.MinioClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jince
 * @date 2025/2/23 16:58
 * @description minio文件存储客户端工厂
 */
@Getter
@Slf4j
public class MinioFileStorageClientFactory extends BaseFileStorageClientFactory<MinioClient> {

    public MinioFileStorageClientFactory(MinioStorageProperties minioStorageProperties) {
        super(minioStorageProperties.getUrl(), minioStorageProperties.getAccessKey(), minioStorageProperties.getSecretKey(), minioStorageProperties.getRegion());
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
            try {
                client.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            log.info("minio客户端工厂关闭了");
//            client = null;
        }
    }
}
