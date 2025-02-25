package com.whim.file;

import com.whim.common.exception.FileStorageException;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jince
 * date: 2025/2/18 14:25
 * description: 文件处理器
 */
@Getter
public class FileHandler {
    private final String fileName;
    private final String storagePath;
    private final String platform;
    private final String platformConfigName;
    private final IFileWrapper fileWrapper;
    private final FileStorageProperties fileStorageProperties;

    private FileHandler(Builder builder) {
        this.fileName = builder.fileName;
        this.storagePath = builder.storagePath;
        this.platform = builder.platform;
        this.fileWrapper = builder.fileWrapper;
        this.platformConfigName = builder.platformConfigName;
        this.fileStorageProperties = builder.fileStorageProperties;
    }

    public static class Builder {
        private final FileStorageProperties fileStorageProperties;
        private final List<IFileAdapter> allFileAdapter;
        private final Map<String, IFileStorage> allFileStorage;
        private String fileName;
        private String storagePath;
        private String platform;
        private String platformConfigName;
        private IFileWrapper fileWrapper;

        public Builder(FileStorageProperties fileStorageProperties, List<IFileAdapter> allFileAdapter, Map<String, IFileStorage> allFileStorage) {
            this.fileStorageProperties = fileStorageProperties;
            this.allFileAdapter = allFileAdapter;
            this.allFileStorage = allFileStorage;
            this.storagePath = fileStorageProperties.getLocal().getFirst().getBasePath();
            this.platform = fileStorageProperties.getDefaultStorage();
        }

        /**
         * 设置文件名称
         *
         * @param fileName 文件名称
         * @return Builder
         */
        public Builder fileName(String fileName) {
            if (fileName == null || fileName.trim().isBlank()) {
                throw new FileStorageException("文件名称不能为空");
            }
            if (fileName.matches("^[\\w\\u4e00-\\u9fa5\\s.\\-]+$")) {
                this.fileName = fileName;
                return this;
            }
            throw new FileStorageException("文件名称格式不正确");
        }


        /**
         * 设置存储地址
         *
         * @param storagePath 存储路径
         * @return Builder
         */
        public Builder storagePath(String storagePath) {
            if (storagePath == null || storagePath.trim().isBlank()) {
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

        /**
         * 设置存储平台
         *
         * @param platform 存储平台
         * @return Builder
         */
        public Builder platform(String platform) {
            if (Objects.isNull(allFileStorage.get(platform))) {
                throw new FileStorageException("未找到对应受支持的平台");
            }
            this.platform = platform;
            return this;
        }

        /**
         * 设置存储平台配置名称
         *
         * @param name 配置名称
         * @return Builder
         */
        @SneakyThrows
        public Builder platformConfigName(String name) {
            if (platform == null || platform.trim().isEmpty()) {
                throw new FileStorageException("必须先指定存储平台(platform)");
            }
            try {
                Field configField = FileStorageProperties.class.getDeclaredField(platform);
                configField.setAccessible(true);

                Object rawConfigList = configField.get(fileStorageProperties);
                if (!(rawConfigList instanceof List)) {
                    throw new FileStorageException("存储平台 '" + platform + "' 的配置类型无效");
                }
                List<FileStorageProperties.StorageConfig> typeSafeList = new ArrayList<>();
                for (Object item : (List<?>) rawConfigList) {
                    if (!(item instanceof FileStorageProperties.StorageConfig)) {
                        throw new FileStorageException("存储平台 '" + platform + "' 的配置项未实现 StorageConfig 接口");
                    }
                    typeSafeList.add((FileStorageProperties.StorageConfig) item);
                }
                boolean exists = typeSafeList.stream().anyMatch(config -> name.equals(config.getName()));
                if (!exists) {
                    throw new FileStorageException("存储平台 '" + platform + "' 中不存在名为 '" + name + "' 的配置");
                }
                this.platformConfigName = name;
                return this;
            } catch (NoSuchFieldException e) {
                throw new FileStorageException("不支持的存储平台: " + platform, e);
            } catch (IllegalAccessException e) {
                throw new FileStorageException("无法访问存储平台配置: " + platform, e);
            }
        }

        /**
         * 设置文件包装器
         *
         * @param file 文件
         */
        protected void fileWrapper(Object file) {
            for (IFileAdapter fileAdapter : allFileAdapter) {
                if (fileAdapter.isSupport(file)) {
                    this.fileWrapper = fileAdapter.getFileWrapper(file);
                    this.fileName = this.fileWrapper.getFileName();
                    return;
                }
            }
            throw new FileStorageException("未找到对应的文件适配器");
        }

        /**
         * 构建文件处理器
         *
         * @return FileHandler 文件处理器
         */
        protected FileHandler build() {
            return new FileHandler(this);
        }
    }
}
