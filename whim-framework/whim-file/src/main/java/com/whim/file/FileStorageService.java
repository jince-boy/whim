package com.whim.file;

import com.whim.core.exception.FileStorageException;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.handler.FileHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.storage.IFileStorage;
import com.whim.file.support.StoragePath;
import com.whim.file.support.FileUploadSecurity;
import lombok.RequiredArgsConstructor;

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
    private final List<IFileStorage> ownedStorages;

    /** 供业务层绑定任务的实际存储实例，调用者不得关闭返回的共享实例。 */
    public IFileStorage getStorage(String platform, String configName) {
        return getStorage(new FileHandler.Builder().platform(platform).platformConfigName(configName).build());
    }

    /** 使用默认存储和随机文件名上传。 */
    public MetaData upload(Object file) {
        return upload(file, null);
    }

    /**
     * 校验实际内容后上传文件，调用配置不能覆盖检测结果。
     *
     * @param file 待上传文件或输入流
     * @param configurator 存储选项配置
     * @return 已保存的文件元数据
     */
    public MetaData upload(Object file, Consumer<FileHandler.Builder> configurator) {
        FileHandler options = buildOptions(configurator);
        IFileStorage storage = getStorage(options);
        try (FileSource source = FileSource.open(file, properties)) {
            String key = getObjectKey(options, source.getFileName());
            FileUploadSecurity.validateMetadata(key, options.getContentType());
            return storage.upload(source, key, source.getContentType());
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

    /**
     * 生成强制二进制附件响应的下载签名，不提前检查对象存在性。
     *
     * @param configurator 存储选项配置
     * @param expiration 签名有效期
     * @return 安全下载签名地址
     */
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

    /**
     * 拒绝没有完成校验环节的单次直传，防止绕过实际内容检查。
     *
     * @param configurator 存储选项配置
     * @param expiration 签名有效期
     * @return 本方法始终拒绝签发并抛出异常
     */
    public String uploadFilePreSignedUrl(Consumer<FileHandler.Builder> configurator, Duration expiration) {
        throw new FileStorageException("单次直传无法校验文件内容，请使用普通上传或分片上传");
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
        for (IFileStorage storage : ownedStorages) {
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
