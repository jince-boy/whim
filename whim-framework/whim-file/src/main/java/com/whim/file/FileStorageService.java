package com.whim.file;

import com.whim.core.exception.FileStorageException;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.handler.FileHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.storage.IFileStorage;
import com.whim.file.support.StoragePath;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 文件存储统一入口，解析调用选项并管理存储实例生命周期。
 */
@RequiredArgsConstructor
public class FileStorageService implements AutoCloseable {
    private final FileStorageProperties properties;
    private final List<IFileStorage> storages;

    /** 使用默认存储和随机文件名上传。 */
    public MetaData upload(Object file) {
        return upload(file, null);
    }

    /** 上传文件；支持 MultipartFile、Path、File、byte[]、InputStream 和受限 URI。 */
    public MetaData upload(Object file, Consumer<FileHandler.Builder> configurator) {
        FileHandler options = buildOptions(configurator);
        IFileStorage storage = getStorage(options);
        try (FileSource source = FileSource.open(file, properties)) {
            String key = getObjectKey(options, source.getFileName());
            String contentType = getContentType(options.getContentType() == null
                    ? source.getContentType() : options.getContentType());
            return storage.upload(source, key, contentType);
        } catch (IOException exception) {
            throw new FileStorageException("关闭上传文件失败", exception);
        }
    }

    /** 打开下载，返回值必须关闭或通过消费方法自动关闭。 */
    public DownloadHandler download(Consumer<FileHandler.Builder> configurator) {
        FileHandler options = buildOptions(configurator);
        return getStorage(options).download(getObjectKey(options, null));
    }

    /** 根据上传返回的元数据打开下载。 */
    public DownloadHandler download(MetaData metadata) {
        return download(builder -> builder.platform(metadata.getPlatform())
                .platformConfigName(metadata.getPlatformConfigName()).objectKey(metadata.getObjectKey()));
    }

    /** 获取文件元数据。 */
    public MetaData getFileMetaData(Consumer<FileHandler.Builder> configurator) {
        FileHandler options = buildOptions(configurator);
        return getStorage(options).getFileMetaData(getObjectKey(options, null));
    }

    /** 判断文件存在，鉴权及存储故障不会转换为 false。 */
    public boolean exists(Consumer<FileHandler.Builder> configurator) {
        FileHandler options = buildOptions(configurator);
        return getStorage(options).exists(getObjectKey(options, null));
    }

    /** 幂等删除文件，不存在也返回 true。 */
    public boolean deleteFile(Consumer<FileHandler.Builder> configurator) {
        FileHandler options = buildOptions(configurator);
        getStorage(options).deleteFile(getObjectKey(options, null));
        return true;
    }

    /** 根据元数据定位并删除文件。 */
    public boolean deleteFile(MetaData metadata) {
        return deleteFile(builder -> builder.platform(metadata.getPlatform())
                .platformConfigName(metadata.getPlatformConfigName()).objectKey(metadata.getObjectKey()));
    }

    /** 生成指定有效期的下载签名；不会提前检查对象是否存在。 */
    public String getFilePreSignedUrl(Consumer<FileHandler.Builder> configurator, Duration expiration) {
        validateExpiration(expiration);
        FileHandler options = buildOptions(configurator);
        return getStorage(options).getFilePreSignedUrl(getObjectKey(options, null), expiration);
    }

    /** 保留原模块的时间单位调用方式。 */
    public String getFilePreSignedUrl(Consumer<FileHandler.Builder> configurator, Integer expire, TimeUnit unit) {
        if (expire == null || unit == null) {
            throw new FileStorageException("签名有效期和时间单位不能为空");
        }
        return getFilePreSignedUrl(configurator, Duration.ofNanos(unit.toNanos(expire)));
    }

    /** 生成 PUT 上传签名；直传大小限制需由桶策略或上传后的业务校验落实。 */
    public String uploadFilePreSignedUrl(Consumer<FileHandler.Builder> configurator, Duration expiration) {
        validateExpiration(expiration);
        FileHandler options = buildOptions(configurator);
        return getStorage(options).uploadFilePreSignedUrl(getObjectKey(options, null),
                getContentType(options.getContentType()), expiration);
    }

    /** 保留原模块的时间单位调用方式。 */
    public String uploadFilePreSignedUrl(Consumer<FileHandler.Builder> configurator, Integer expire, TimeUnit unit) {
        if (expire == null || unit == null) {
            throw new FileStorageException("签名有效期和时间单位不能为空");
        }
        return uploadFilePreSignedUrl(configurator, Duration.ofNanos(unit.toNanos(expire)));
    }

    /** 配置函数执行完毕后再生成选项，避免提前解析默认平台。 */
    private FileHandler buildOptions(Consumer<FileHandler.Builder> configurator) {
        FileHandler.Builder builder = new FileHandler.Builder();
        if (configurator != null) {
            configurator.accept(builder);
        }
        return builder.build();
    }

    /** 显式选择配置优先；切换平台后使用该平台首个配置。 */
    private IFileStorage getStorage(FileHandler options) {
        String platform = options.getPlatform() == null ? properties.getDefaultStorage() : options.getPlatform();
        String configName = options.getPlatformConfigName();
        if (configName == null && platform.equals(properties.getDefaultStorage())) {
            configName = properties.getDefaultConfigName();
        }
        for (IFileStorage storage : storages) {
            if (storage.getPlatform().equals(platform)
                    && (configName == null || storage.getConfigName().equals(configName))) {
                return storage;
            }
        }
        throw new FileStorageException("未找到指定的文件存储配置");
    }

    /** 将完整 key 或目录加文件名统一转换为相对 key，拒绝歧义参数。 */
    private String getObjectKey(FileHandler options, String defaultName) {
        if (options.getObjectKey() != null) {
            if (options.getFileName() != null || options.getStoragePath() != null) {
                throw new FileStorageException("对象 key 不能与文件名或存储目录同时设置");
            }
            return StoragePath.validateKey(options.getObjectKey());
        }
        String fileName = options.getFileName() == null ? defaultName : options.getFileName();
        StoragePath.validateKey(fileName);
        if (fileName.contains("/")) {
            throw new FileStorageException("文件名不能包含目录");
        }
        String directory = options.getStoragePath();
        return StoragePath.validateKey(directory == null || directory.isEmpty()
                ? fileName : directory + "/" + fileName);
    }

    /** 校验 MIME 语法，空缺时使用二进制默认值，不信任客户端声明的内容安全性。 */
    private String getContentType(String contentType) {
        if (contentType == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        try {
            if (contentType.isBlank() || contentType.chars().anyMatch(Character::isISOControl)) {
                throw new FileStorageException("文件内容类型无效");
            }
            MediaType parsed = MediaType.parseMediaType(contentType);
            if (parsed.isWildcardType() || parsed.isWildcardSubtype()) {
                throw new FileStorageException("文件内容类型不能包含通配符");
            }
            return parsed.toString();
        } catch (IllegalArgumentException exception) {
            throw new FileStorageException("文件内容类型无效", exception);
        }
    }

    /** 统一签名有效期为 1 秒至 7 天的整数秒。 */
    private void validateExpiration(Duration expiration) {
        if (expiration == null || expiration.compareTo(Duration.ofSeconds(1)) < 0
                || expiration.compareTo(Duration.ofDays(7)) > 0 || expiration.getNano() != 0) {
            throw new FileStorageException("签名有效期必须为 1 秒至 7 天的整数秒");
        }
    }

    /** 释放所有存储实例；一个实例失败不会阻止其余实例清理。 */
    @Override
    public void close() {
        FileStorageException failure = null;
        for (IFileStorage storage : storages) {
            try {
                storage.close();
            } catch (Exception exception) {
                if (failure == null) {
                    failure = new FileStorageException("关闭文件存储资源失败", exception);
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
