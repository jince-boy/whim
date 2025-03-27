package com.whim.file.handler;

import com.whim.common.exception.FileStorageException;
import com.whim.file.adapter.IFileAdapter;
import com.whim.file.config.FileStorageProperties;
import com.whim.file.storage.IFileStorage;
import com.whim.file.wrapper.BaseFileWrapper;
import com.whim.file.wrapper.IFileWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author jince
 * date: 2025/2/18 14:25
 * description: 文件选项类
 */
@Getter
@Slf4j
public class FileHandler {
    private final String platform;
    private final String platformConfigName;
    private final String fileName;
    private final String storagePath;
    private final String contentType;
    private final IFileWrapper fileWrapper;
    private final Object storageProperties;

    private FileHandler(FileHandler.Builder builder) {
        this.platform = builder.platform;
        this.platformConfigName = builder.platformConfigName;
        this.fileWrapper = builder.fileWrapper;
        this.storageProperties = builder.storageProperties;
        this.fileName = builder.fileName;
        this.storagePath = builder.storagePath;
        this.contentType = builder.contentType;
    }

    public interface PublicBuilder {
        PublicBuilder fileName(String fileName);

        PublicBuilder contentType(String contentType);

        PublicBuilder storagePath(String storagePath);

        PublicBuilder platform(String platform);

        PublicBuilder platformConfigName(String name);
    }

    public static final class Builder implements PublicBuilder {
        private final FileStorageProperties fileStorageProperties;
        private final List<IFileAdapter> allFileAdapter;
        private final Map<String, IFileStorage> allFileStorage;
        private String platform;
        private String platformConfigName;
        private IFileWrapper fileWrapper;
        private Object storageProperties;
        private String fileName;
        private String storagePath;
        private String contentType;

        public Builder(FileStorageProperties fileStorageProperties, List<IFileAdapter> allFileAdapter, Map<String, IFileStorage> allFileStorage) {
            this.fileStorageProperties = fileStorageProperties;
            this.allFileAdapter = allFileAdapter;
            this.allFileStorage = allFileStorage;
            this.platform = fileStorageProperties.getDefaultStorage();
            initializePlatformConfig();
        }

        @Override
        public PublicBuilder fileName(String fileName) {
            if (fileName == null || fileName.trim().isBlank()) {
                throw new FileStorageException("文件名称不能为空");
            }
            if (fileName.matches("^[\\w\\u4e00-\\u9fa5\\s.\\-]+$")) {
                this.fileName = fileName;
                return this;
            }
            throw new FileStorageException("文件名称格式不正确");
        }

        @Override
        public PublicBuilder contentType(String contentType) {
            if (contentType == null || contentType.trim().isBlank()) {
                throw new FileStorageException("文件内容类型不能为空");
            }
            this.contentType = contentType;
            return this;
        }

        @Override
        public PublicBuilder storagePath(String storagePath) {
            if (storagePath == null || storagePath.trim().isEmpty()) {
                throw new FileStorageException("文件路径不能为空");
            }
            try {
                Paths.get(storagePath); // 验证路径格式
            } catch (InvalidPathException e) {
                throw new FileStorageException("存储路径格式无效: " + storagePath, e);
            }
            this.storagePath = storagePath;
            return this;
        }

        @Override
        public PublicBuilder platform(String platform) {
            if (!allFileStorage.containsKey(platform)) {
                throw new FileStorageException("未找到对应受支持的平台");
            }
            this.platform = platform;
            this.initializePlatformConfig();
            return this;
        }

        @Override
        public PublicBuilder platformConfigName(String name) {
            List<? extends FileStorageProperties.StorageConfig> configs = getCurrentPlatformConfigs();
            FileStorageProperties.StorageConfig target = configs.stream()
                    .filter(c -> c.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new FileStorageException("在平台 " + platform + " 中找不到配置: " + name));
            this.platformConfigName = name;
            this.storageProperties = target;
            return this;
        }

        public void fileWrapper(Object file) {
            for (IFileAdapter fileAdapter : allFileAdapter) {
                if (fileAdapter.isSupport(file)) {
                    this.fileWrapper = fileAdapter.getFileWrapper(file);
                    this.contentType = ((BaseFileWrapper<?>) this.fileWrapper).getDefaultContentType();
                    this.fileName = ((BaseFileWrapper<?>) this.fileWrapper).getDefaultFileName();
                    return;
                }
            }
            throw new FileStorageException("未找到对应的文件适配器");
        }

        public FileHandler build() {
            return new FileHandler(this);
        }

        /**
         * 初始化平台配置
         * 此方法用于加载和初始化当前平台的存储配置如果平台没有配置，则抛出异常
         * 它会选择第一个可用的配置作为默认配置，并设置相关属性
         */
        private void initializePlatformConfig() {
            // 获取当前平台的所有存储配置
            List<? extends FileStorageProperties.StorageConfig> configs = getCurrentPlatformConfigs();
            // 如果配置列表为空，则抛出异常，表示没有可用的配置
            if (configs.isEmpty()) {
                throw new FileStorageException("平台 " + platform + " 没有可用配置");
            }
            // 默认使用第一个配置
            FileStorageProperties.StorageConfig defaultConfig = configs.getFirst();
            // 设置当前配置的名称
            this.platformConfigName = defaultConfig.getName();
            // 设置当前的存储属性
            this.storageProperties = defaultConfig;
        }

        @SuppressWarnings("unchecked")
        private List<? extends FileStorageProperties.StorageConfig> getCurrentPlatformConfigs() {
            try {
                // 获取FileStorageProperties类中与当前平台对应的字段
                Field field = FileStorageProperties.class.getDeclaredField(platform);
                // 设置字段可访问，即使它是私有的
                field.setAccessible(true);
                // 从fileStorageProperties实例中获取字段值，并进行类型转换
                return (List<? extends FileStorageProperties.StorageConfig>) field.get(fileStorageProperties);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // 如果发生反射操作错误，抛出表示存储平台配置无效的自定义异常
                throw new FileStorageException("无效的存储平台配置: " + platform, e);
            }
        }
    }
}