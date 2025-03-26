package com.whim.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jince
 * date: 2025/2/18 15:46
 * description: 文件存储配置
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
    private List<AliYunOssStorageProperties> aliyunOss = new ArrayList<>();

    public interface StorageConfig {
        String getName();

        String getBasePath();
    }

    @Data
    public static abstract class BaseStorageProperties implements StorageConfig {
        String name;
        String url;
        String accessKey;
        String secretKey;
        String bucket;
        String basePath;
        String region;
    }

    @Data
    public static class LocalStorageProperties implements StorageConfig {
        private String name;
        private String basePath;
    }

    public static class MinioStorageProperties extends BaseStorageProperties implements StorageConfig {
    }

    public static class AliYunOssStorageProperties extends BaseStorageProperties implements StorageConfig {
    }
}
