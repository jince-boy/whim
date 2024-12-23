package com.whim.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Jince
 * date: 2024/11/15 22:15
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
    private LocalStorageProperties local = new LocalStorageProperties("whim/fil112112e");

    @Data
    @AllArgsConstructor
    public static class LocalStorageProperties {
        private String storagePath;
    }

}
