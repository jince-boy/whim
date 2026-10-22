package com.whim.filemanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 业务上传容量、有效期和清理策略，与普通上传大小限制分离。
 */
@Getter
@Setter
@ConfigurationProperties("file.upload")
public class FileUploadProperties {
    private DataSize maximumFileSize = DataSize.ofGigabytes(1);
    private DataSize partSize = DataSize.ofMegabytes(8);
    private Duration taskLifetime = Duration.ofHours(24);
    private Duration signatureLifetime = Duration.ofMinutes(5);
    private int concurrency = 3;
    private int maximumActiveTasks = 10;
    private DataSize maximumPendingBytes = DataSize.ofGigabytes(5);
}

