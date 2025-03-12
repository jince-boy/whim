package com.whim.file.storage.impl;

import com.whim.common.exception.FileStorageException;
import com.whim.common.utils.FileUtil;
import com.whim.file.FileOptions;
import com.whim.file.client.MinioFileStorageClientFactory;
import com.whim.file.config.FileStorageProperties.MinioStorageProperties;
import com.whim.file.model.vo.FileInfoVO;
import com.whim.file.storage.IFileStorage;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author jince
 * date: 2025/2/23 16:31
 * description: minio文件存储实现
 */
@Slf4j
@Component("minio")
public class MinioFileStorageImpl implements IFileStorage {
    /**
     * 上传文件到Minio存储服务
     *
     * @param fileOptions 文件上传配置参数，包含：
     *                    - 存储路径配置(storageProperties)
     *                    - 文件存储路径(storagePath)
     *                    - 文件名(fileName)
     *                    - 文件包装对象(fileWrapper)包含文件流、大小和类型信息
     * @throws FileStorageException 当Minio上传过程中发生异常时抛出
     */
    @Override
    public FileInfoVO upload(FileOptions fileOptions) {
        // 获取Minio专用存储配置并创建自动关闭的客户端工厂
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();
        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            // 构建完整存储路径：基础路径 + 存储子路径 + 文件名
            Path path = Paths.get(FileUtil.joinPath(storageProperties.getBasePath(), fileOptions.getStoragePath()));
            Path resolve = path.resolve(fileOptions.getFileName());
            try {
                // 执行Minio文件上传操作
                fileStorageClientFactory.getClient().putObject(
                        PutObjectArgs.builder()
                                .bucket(storageProperties.getBucket())
                                .object(resolve.toString())
                                .stream(fileOptions.getFileWrapper().getInputStream(), fileOptions.getFileWrapper().getFileSize(), -1)
                                .contentType(fileOptions.getFileWrapper().getContentType())
                                .build()
                );
                return new FileInfoVO(fileOptions.getFileName(), path.toString(), fileOptions.getPlatform(), FileUtils.byteCountToDisplaySize(fileOptions.getFileWrapper().getFileSize()), fileOptions.getFileWrapper().getContentType());
            } catch (Exception e) {
                // 将Minio客户端异常转换为统一的存储异常
                throw new FileStorageException("minio保存文件发生错误", e);
            }
        }
    }

    @Override
    public InputStream getFile(FileOptions fileOptions) {
        MinioStorageProperties storageProperties = (MinioStorageProperties) fileOptions.getStorageProperties();

        try (MinioFileStorageClientFactory fileStorageClientFactory = new MinioFileStorageClientFactory(storageProperties)) {
            String path = FileUtil.joinPath(storageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName());
            log.info(path);
            return fileStorageClientFactory.getClient()
                    .getObject(
                            GetObjectArgs.builder()
                                    .bucket(storageProperties.getBucket())
                                    .object(path)
                                    .build()
                    );
        } catch (ErrorResponseException e) {
            // Minio 返回的错误响应（如文件不存在或权限不足）
            throw new FileStorageException("文件获取失败：文件不存在或权限不足", e);
        } catch (ServerException | InternalException e) {
            // Minio 服务器内部错误
            throw new FileStorageException("文件获取失败：服务器内部错误", e);
        } catch (InsufficientDataException e) {
            // 数据不完整异常
            throw new FileStorageException("文件获取失败：数据不完整", e);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            // 密钥或算法无效
            throw new FileStorageException("文件获取失败：密钥或算法无效", e);
        } catch (InvalidResponseException | XmlParserException e) {
            // 响应无效或 XML 解析错误
            throw new FileStorageException("文件获取失败：响应无效或解析错误", e);
        } catch (IOException e) {
            // IO 异常
            throw new FileStorageException("文件获取失败：IO 错误", e);
        } catch (Exception e) {
            // 其他未知异常
            throw new FileStorageException("文件获取失败：未知错误", e);
        }
    }
}
