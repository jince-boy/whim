package com.whim.file.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 文件存储配置，密钥不参与字符串输出。
 */
@Getter
@Setter
@Validated
@ConfigurationProperties("file.storage")
public class FileStorageProperties {
    private boolean enabled = true;
    @NotBlank
    private String defaultStorage = "local";
    private String defaultConfigName;
    @NotNull
    private DataSize maxFileSize = DataSize.ofMegabytes(20);
    @NotNull
    private Duration connectTimeout = Duration.ofSeconds(10);
    /** MinIO 读写及远程 URI 读取等待时限；OSS 使用 requestTimeout 限制完整请求。 */
    @NotNull
    private Duration readTimeout = Duration.ofSeconds(30);
    /** 云存储单次 HTTP 请求总时限；普通上传另在输入读取前后检查同样的时间预算。 */
    @NotNull
    private Duration requestTimeout = Duration.ofMinutes(5);
    @NotNull
    private List<@Valid LocalStorageProperties> local = new ArrayList<>();
    @NotNull
    private List<@Valid CloudStorageProperties> minio = new ArrayList<>();
    @NotNull
    private List<@Valid CloudStorageProperties> aliyunOss = new ArrayList<>();
    /** 允许抓取的精确主机名；空集合禁用 URI 上传，不支持通配符及重定向。 */
    @NotNull
    private Set<String> remoteAllowedHosts = Set.of();

    /**
     * @author Jince
     * @date 2026/10/22
     * @description 本地存储实例配置。
     */
    @Getter
    @Setter
    public static class LocalStorageProperties {
        @NotBlank
        private String name;
        @NotBlank
        private String basePath;
    }

    /**
     * @author Jince
     * @date 2026/10/22
     * @description 对象存储实例配置，OSS 必须指定地域。
     */
    @Getter
    @Setter
    public static class CloudStorageProperties {
        @NotBlank
        private String name;
        @NotBlank
        private String url;
        @NotBlank
        private String accessKey;
        @NotBlank
        private String secretKey;
        @NotBlank
        private String bucket;
        private String basePath = "";
        private String region;
    }
}
