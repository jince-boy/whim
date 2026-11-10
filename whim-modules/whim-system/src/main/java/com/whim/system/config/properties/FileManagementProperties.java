package com.whim.system.config.properties;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件管理凭据加密与上传分片规则配置。
 */
@Getter
@Setter
@Validated
@ConfigurationProperties("file.management")
public class FileManagementProperties {
    /** Base64 编码的32字节密钥，由环境注入，不写入数据库。 */
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
            // 下限递增避免档位重叠；分片下限兼容S3，上限控制当前服务端整片读取的内存占用。
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
}
