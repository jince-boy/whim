package com.whim.file.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jince
 * date: 2025/6/24 12:13
 * description: 文件存储属性
 */
@Data
@ConfigurationProperties(prefix = "file.storage")
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
