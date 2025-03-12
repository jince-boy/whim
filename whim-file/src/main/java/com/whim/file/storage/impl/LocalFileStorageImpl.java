package com.whim.file.storage.impl;

import com.whim.common.exception.FileStorageException;
import com.whim.common.utils.FileUtil;
import com.whim.file.FileOptions;
import com.whim.file.config.FileStorageProperties.LocalStorageProperties;
import com.whim.file.model.vo.FileInfoVO;
import com.whim.file.storage.IFileStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author jince
 * date: 2025/2/18 17:48
 * description: 本地文件存储实现
 */
@Component("local")
@Slf4j
public class LocalFileStorageImpl implements IFileStorage {
    /**
     * 实现文件上传到本地存储的具体逻辑（覆盖父类方法）
     *
     * @param fileOptions 文件操作参数对象，包含：
     *                    - storageProperties: 存储配置信息（需强转为LocalStorageProperties）
     *                    - storagePath: 相对于基础路径的存储目录
     *                    - fileName: 目标文件名
     *                    - fileWrapper: 包含文件输入流的包装对象
     * @throws FileStorageException 当发生IO操作异常时抛出
     */
    @Override
    public FileInfoVO upload(FileOptions fileOptions) {
        // 解析本地存储配置并构建基础路径
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        Path basePath = FileUtil.generateAbsolutePath(FileUtil.joinPath(localStorageProperties.getBasePath(), fileOptions.getStoragePath()));
        try {
            // 创建目标目录（若不存在）
            Files.createDirectories(basePath);
            // 构建完整文件路径并标准化处理
            Path filePath = basePath.resolve(fileOptions.getFileName()).normalize();
            // 执行文件复制操作（自动覆盖已存在文件）
            Files.copy(fileOptions.getFileWrapper().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            // 记录存储配置信息
            log.info(localStorageProperties.toString());
            return new FileInfoVO(fileOptions.getFileName(), basePath.toString(), fileOptions.getPlatform(), FileUtils.byteCountToDisplaySize(fileOptions.getFileWrapper().getFileSize()), fileOptions.getFileWrapper().getContentType());
        } catch (IOException e) {
            // 将IO异常转换为领域异常抛出
            throw new FileStorageException(e);
        }
    }

    @Override
    public InputStream getFile(FileOptions fileOptions) {
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        Path path = FileUtil.generateAbsolutePath(FileUtil.joinPath(localStorageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName()));
        if (!Files.exists(path)) {
            throw new FileStorageException("文件不存在" + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new FileStorageException("路径指向非文件" + path);
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new FileStorageException("文件流打开失败" + path, e);
        }
    }
}
