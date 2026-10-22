package com.whim.file.config;

import com.whim.core.exception.FileStorageException;
import com.whim.file.FileStorageService;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.storage.IFileStorage;
import com.whim.file.storage.impl.AliyunOssFileStorageImpl;
import com.whim.file.storage.impl.LocalFileStorageImpl;
import com.whim.file.storage.impl.MinioFileStorageImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 文件存储自动配置，启动时校验配置并注册内置及自定义存储实例。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "file.storage", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FileStorageProperties.class)
@RequiredArgsConstructor
public class FileStorageAutoConfiguration {
    private final FileStorageProperties properties;

    /** 注册统一服务；仅负责内置客户端的生命周期，自定义实例交由 Spring 管理。 */
    @Bean
    @ConditionalOnMissingBean
    public FileStorageService fileStorageService(ObjectProvider<IFileStorage> customStorages) {
        if (properties.getMaxFileSize().toBytes() <= 0
                || properties.getMaxFileSize().toBytes() > 5L * 1024 * 1024 * 1024) {
            throw new FileStorageException("单文件大小上限必须大于零且不超过 5 GiB");
        }
        for (Duration timeout : List.of(properties.getConnectTimeout(), properties.getReadTimeout(), properties.getRequestTimeout())) {
            if (timeout.toMillis() < 1 || timeout.toMillis() > Integer.MAX_VALUE) {
                throw new FileStorageException("文件存储超时必须为 1 毫秒至 2147483647 毫秒");
            }
        }
        List<IFileStorage> storages = new ArrayList<>();
        List<IFileStorage> ownedStorages = new ArrayList<>();
        try {
            properties.getLocal().forEach(config -> ownedStorages.add(new LocalFileStorageImpl(config)));
            properties.getMinio().forEach(config -> ownedStorages.add(new MinioFileStorageImpl(config, properties)));
            properties.getAliyunOss().forEach(config -> ownedStorages.add(new AliyunOssFileStorageImpl(config, properties)));
            storages.addAll(ownedStorages);
            storages.addAll(customStorages.orderedStream().toList());
            Set<String> identifiers = new HashSet<>();
            boolean defaultFound = false;
            for (IFileStorage storage : storages) {
                if (storage.getPlatform() == null || !storage.getPlatform().matches("[a-zA-Z][a-zA-Z0-9]*")
                        || storage.getConfigName() == null || !storage.getConfigName().matches("[a-zA-Z0-9_-]+")) {
                    throw new FileStorageException("存储平台或配置名称格式无效");
                }
                if (!identifiers.add(storage.getPlatform() + ":" + storage.getConfigName())) {
                    throw new FileStorageException("同一存储平台内存在重复配置名称");
                }
                if (storage.getPlatform().equals(properties.getDefaultStorage())
                        && (properties.getDefaultConfigName() == null
                        || storage.getConfigName().equals(properties.getDefaultConfigName()))) {
                    defaultFound = true;
                }
            }
            if (!defaultFound) {
                throw new FileStorageException("默认文件存储平台或配置不存在");
            }
            return new FileStorageService(properties, List.copyOf(storages), List.copyOf(ownedStorages));
        } catch (RuntimeException exception) {
            // 创建或校验失败时只回收已经创建的内置实例。
            for (IFileStorage storage : ownedStorages) {
                try {
                    storage.close();
                } catch (Exception closeException) {
                    exception.addSuppressed(closeException);
                }
            }
            if (exception instanceof FileStorageException storageException) {
                throw storageException;
            }
            throw new FileStorageException("初始化文件存储配置失败", exception);
        }
    }
}
