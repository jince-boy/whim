package com.whim.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jince
 * date: 2025/2/18 15:46
 * description:
 */
@Component
@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageProperties {
    /**
     * 默认存储平台
     */
    private String defaultStorage = "local";

    /**
     * 本地存储配置
     */
    private List<LocalStorageProperties> local = new ArrayList<>();
    private List<MinioStorageProperties> minio = new ArrayList<>();

    public interface StorageConfig {
        String getName();

        String getBasePath();
    }

    @Data
    public static class LocalStorageProperties implements StorageConfig {
        private String name;
        private String basePath;
    }

    @Data
    public static class MinioStorageProperties implements StorageConfig {
        private String name;
        private String url;
        private String accessKey;
        private String secretKey;
        private String bucket;
        private String basePath;
    }
}
