package com.whim.file.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private DataSize maxFileSize = DataSize.ofMegabytes(200);
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

    /** 存储凭据的加密主密钥，Base64编码的32字节密钥，由环境注入，不写入数据库。 */
    private String secretKey;

    /** 按文件大小下限递增排列，命中最后一个不超过文件大小的档位。 */
    private List<UploadPartSizeRule> uploadPartSizeRules = List.of(
            new UploadPartSizeRule(DataSize.ofBytes(0), DataSize.ofMegabytes(8)),
            new UploadPartSizeRule(DataSize.ofMegabytes(100), DataSize.ofMegabytes(16)),
            new UploadPartSizeRule(DataSize.ofGigabytes(1), DataSize.ofMegabytes(32)),
            new UploadPartSizeRule(DataSize.ofGigabytes(10), DataSize.ofMegabytes(64)));

    /**
     * 校验分档规则覆盖所有文件大小且满足分片兼容性和内存限制。
     *
     * @return 分档规则是否合法
     */
    @AssertTrue(message = "上传分片规则必须从0B开始按文件大小严格递增，且每片大小须在5MB至64MB之间")
    public boolean isUploadPartSizeRulesValid() {
        if (uploadPartSizeRules == null || uploadPartSizeRules.isEmpty()) return false;
        long previousMinimum = -1;
        for (UploadPartSizeRule rule : uploadPartSizeRules) {
            if (rule == null || rule.getMinFileSize() == null || rule.getPartSize() == null) return false;
            long minimumFileSize = rule.getMinFileSize().toBytes();
            long partSize = rule.getPartSize().toBytes();
            // 下限递增避免档位重叠；分片下限兼容S3，上限控制整片读取的内存占用。
            if (minimumFileSize <= previousMinimum
                    || partSize < DataSize.ofMegabytes(5).toBytes()
                    || partSize > DataSize.ofMegabytes(64).toBytes()) return false;
            previousMinimum = minimumFileSize;
        }
        return uploadPartSizeRules.getFirst().getMinFileSize().toBytes() == 0;
    }

    /**
     * @author Jince
     * @date 2026/11/10
     * @description 一个文件大小档位及其对应的上传分片大小。
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadPartSizeRule {
        /** 档位包含的文件大小下限。 */
        private DataSize minFileSize;
        /** 当前档位的单片大小，最后一片按剩余字节数上传。 */
        private DataSize partSize;
    }

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
