package com.whim.file.storage.impl;

import com.whim.common.exception.FileStorageException;
import com.whim.common.utils.PathUtil;
import com.whim.file.FileOptions;
import com.whim.file.client.MinioFileStorageClientFactory;
import com.whim.file.config.FileStorageProperties.MinioStorageProperties;
import com.whim.file.model.FileInfo;
import com.whim.file.storage.IFileStorage;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

/**
 * @author jince
 * date: 2025/2/23 16:31
 * description: minio文件存储实现
 */
@Slf4j
@Component("minio")
@RequiredArgsConstructor
public class MinioFileStorageImpl implements IFileStorage {
    private final Tika tika;

    /**
     * 上传文件到Minio
     *
     * @param fileOptions 文件选项
     * @return true 上传成功，false 上传失败
     */
    @Override
    public Boolean upload(FileOptions fileOptions) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtil.mergePath(PathUtil.SlashType.FORWARD_SLASH, false, storageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName());
            fileStorageClientFactory.getClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(path)
                            .stream(fileOptions.getFileWrapper().getInputStream(), -1, 5 * 1024 * 1024)
                            .build()
            );
            return true;
        } catch (Exception e) {
            throw new FileStorageException("文件上传失败", e);
        }
    }

    /**
     * 获取Minio中的文件信息
     *
     * @param fileOptions 文件选项
     * @return 文件信息
     */
    @Override
    public FileInfo getFileInfo(FileOptions fileOptions) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtil.mergePath(PathUtil.SlashType.FORWARD_SLASH, false, storageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName());
            GetObjectResponse object = fileStorageClientFactory.getClient()
                    .getObject(
                            GetObjectArgs.builder()
                                    .bucket(storageProperties.getBucket())
                                    .object(path)
                                    .build()
                    );
            InputStream inputStream = new BufferedInputStream(object);
            String contentType = tika.detect(inputStream);
            inputStream.reset();
            return new FileInfo()
                    .setFileName(fileOptions.getFileName())
                    .setFileSize(FileUtils.byteCountToDisplaySize(Long.valueOf(Objects.requireNonNull(object.headers().get("Content-Length")))))
                    .setStoragePath(path)
                    .setPlatform(fileOptions.getPlatform())
                    .setPlatformConfigName(fileOptions.getPlatformConfigName())
                    .setInputStream(inputStream)
                    .setContentType(contentType)
                    .setUploadTime(LocalDateTime.ofInstant(Objects.requireNonNull(object.headers().getDate("Date")).toInstant(), ZoneId.systemDefault()));
        } catch (ErrorResponseException e) {
            throw new FileStorageException("文件不存在", e);
        } catch (Exception e) {
            throw new FileStorageException("文件获取失败", e);
        }
    }

    /**
     * 删除Minio中的文件
     *
     * @param fileOptions 文件选项
     * @return true 删除成功，false 删除失败
     */
    @Override
    public Boolean deleteFile(FileOptions fileOptions) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtil.mergePath(PathUtil.SlashType.FORWARD_SLASH,
                    false, storageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName());
            fileStorageClientFactory.getClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(path)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            throw new FileStorageException("文件不存在", e);
        } catch (Exception e) {
            throw new FileStorageException("删除文件失败", e);
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param fileOptions 文件选项
     * @return true 文件存在，false 文件不存在
     */
    @Override
    public Boolean exists(FileOptions fileOptions) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtil.mergePath(PathUtil.SlashType.FORWARD_SLASH, false, storageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName());
            fileStorageClientFactory.getClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .object(path)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            throw new FileStorageException("获取文件信息失败", e);
        }
    }

    /**
     * 获取Minio中的文件列表
     *
     * @param fileOptions 文件选项
     * @return 文件名称列表
     */
    @Override
    public List<String> list(FileOptions fileOptions) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtil.mergePath(PathUtil.SlashType.FORWARD_SLASH, false, storageProperties.getBasePath(), fileOptions.getStoragePath());
            Iterable<Result<Item>> results = fileStorageClientFactory.getClient().listObjects(
                    ListObjectsArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .prefix(path)  // 指定文件夹路径（可选）
                            .recursive(true)     // 递归获取所有文件（包括子文件夹）
                            .build()
            );
            // 获取所有文件名
            return StreamSupport.stream(results.spliterator(), false)
                    .map(result -> {
                        try {
                            return result.get().objectName();
                        } catch (Exception e) {
                            throw new RuntimeException("获取文件失败", e);
                        }
                    })
                    .sorted() // 按字母排序
                    .toList();
        } catch (Exception e) {
            throw new FileStorageException("获取文件列表失败", e);
        }
    }

    /**
     * 获取Minio中的文件预签名URL
     *
     * @param fileOptions 文件选项
     * @return 文件预签名URL
     */
    @Override
    public String getFilePreSignedUrl(FileOptions fileOptions) {
        return getFilePreSignedUrl(fileOptions, null, null); // 默认不设置有效时间
    }

    /**
     * 获取Minio中的文件预签名URL
     *
     * @param fileOptions 文件选项
     * @param expire      有效期
     * @param timeUnit    有效期单位
     * @return 文件预签名URL
     */
    @Override
    public String getFilePreSignedUrl(FileOptions fileOptions, Integer expire, TimeUnit timeUnit) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtil.mergePath(PathUtil.SlashType.FORWARD_SLASH, false, storageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName());
            GetPresignedObjectUrlArgs.Builder builder = GetPresignedObjectUrlArgs.builder()
                    .bucket(storageProperties.getBucket())
                    .object(path)
                    .method(Method.GET);
            // 如果设置了有效时间，则添加到参数中
            if (expire != null && timeUnit != null) {
                builder.expiry(expire, timeUnit);
            }
            return fileStorageClientFactory.getClient().getPresignedObjectUrl(builder.build());
        } catch (ErrorResponseException e) {
            throw new FileStorageException("文件不存在", e);
        } catch (Exception e) {
            throw new FileStorageException("获取文件信息失败", e);
        }
    }

    /**
     * 获取上传文件预签名URL
     *
     * @param fileOptions 文件选项
     * @param expire      有效期
     * @param timeUnit    有效期单位
     * @return 文件预签名URL
     */
    @Override
    public String uploadFilePreSignedUrl(FileOptions fileOptions, Integer expire, TimeUnit timeUnit) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = PathUtil.mergePath(PathUtil.SlashType.FORWARD_SLASH, false, storageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName());
            return fileStorageClientFactory.getClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(storageProperties.getBucket())
                            .expiry(expire, timeUnit)
                            .object(path)
                            .method(Method.PUT)
                            .build()
            );
        } catch (Exception e) {
            throw new FileStorageException("获取上传文件预签名url失败", e);
        }
    }
}
