package com.whim.filemanagement.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 业务上传的独立事务边界及清理调度配置。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FileUploadProperties.class)
@EnableScheduling
@ConditionalOnProperty(prefix = "file.storage", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileUploadConfiguration {
    /** 状态阶段必须独立提交，避免外部事务回滚时丢失已经发生的对象存储操作。 */
    @Bean
    @Qualifier("fileUploadTransactionTemplate")
    public TransactionTemplate fileUploadTransactionTemplate(PlatformTransactionManager manager,
                                                               FileUploadProperties properties) {
        long partSize = properties.getPartSize().toBytes();
        if (properties.getMaximumActiveTasks() < 1 || properties.getMaximumActiveTasks() > 100
                || properties.getMaximumPendingBytes().toBytes() < properties.getMaximumFileSize().toBytes()) {
            throw new IllegalArgumentException("单用户活动任务限制必须为1至100，待上传容量不得小于单文件上限");
        }
        if (partSize < 5L * 1024 * 1024 || partSize > 16L * 1024 * 1024
                || properties.getMaximumFileSize().toBytes() < 1
                || properties.getMaximumFileSize().toBytes() > partSize * 10000) {
            throw new IllegalArgumentException("分片大小必须为5至16MiB，文件上限不得超过一万个分片");
        }
        if (properties.getConcurrency() < 1 || properties.getConcurrency() > 6
                || properties.getTaskLifetime().compareTo(Duration.ofHours(1)) < 0
                || properties.getTaskLifetime().compareTo(Duration.ofDays(7)) > 0
                || properties.getSignatureLifetime().compareTo(Duration.ofSeconds(30)) < 0
                || properties.getSignatureLifetime().compareTo(Duration.ofMinutes(15)) > 0) {
            throw new IllegalArgumentException("上传并发必须为1至6，任务有效期为1小时至7天，签名有效期为30秒至15分钟");
        }
        TransactionTemplate template = new TransactionTemplate(manager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setTimeout(300);
        return template;
    }
}

