package com.whim.file.storage.impl;

import com.whim.common.exception.FileStorageException;
import com.whim.common.utils.PathUtil;
import com.whim.file.FileOptions;
import com.whim.file.config.FileStorageProperties.LocalStorageProperties;
import com.whim.file.model.FileInfo;
import com.whim.file.storage.IFileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author jince
 * date: 2025/2/18 17:48
 * description: 本地文件存储实现
 */
@Component("local")
@Slf4j
@RequiredArgsConstructor
public class LocalFileStorageImpl implements IFileStorage {
    private final Tika tika;

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
    public Boolean upload(FileOptions fileOptions) {
        // 解析本地存储配置并构建基础路径
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        Path basePath = Paths.get(PathUtil.mergePath(PathUtil.SlashType.BACK_SLASH, true, localStorageProperties.getBasePath(), fileOptions.getStoragePath())).toAbsolutePath();
        try {
            // 创建目标目录（若不存在）
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }
            // 构建完整文件路径并标准化处理
            Path filePath = basePath.resolve(fileOptions.getFileName()).normalize();
            // 执行文件复制操作（自动覆盖已存在文件）
            return Files.copy(fileOptions.getFileWrapper().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING) > 0;
        } catch (IOException e) {
            // 将IO异常转换为领域异常抛出
            throw new FileStorageException(e);
        }
    }

    /**
     * 获取文件信息
     * 根据提供的文件选项，检索并构建文件信息对象
     *
     * @param fileOptions 文件选项，包含文件的存储路径、文件名等信息
     * @return FileInfo对象，包含文件的详细信息，如文件大小、内容类型等
     * @throws FileStorageException 当文件不存在或文件操作失败时抛出异常
     */
    @Override
    public FileInfo getFileInfo(FileOptions fileOptions) {
        // 获取文件存储属性
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        // 构造文件的绝对路径
        Path path = Paths.get(PathUtil.mergePath(PathUtil.SlashType.BACK_SLASH, true, localStorageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName())).toAbsolutePath();
        try {
            // 一次性获取文件属性，减少系统调用
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            long fileSize = attrs.size();
            FileTime lastModifiedTime = attrs.lastModifiedTime();
            // 使用 BufferedInputStream 支持 mark/reset
            InputStream fileStream = new BufferedInputStream(Files.newInputStream(path));
            String contentType;
            try {
                fileStream.mark(Integer.MAX_VALUE); // 标记起始位置
                contentType = tika.detect(fileStream);
                fileStream.reset(); // 重置到起始位置，供后续读取
            } catch (IOException e) {
                fileStream.close(); // 确保异常时关闭流
                throw new FileStorageException("检测 MIME 类型失败: " + path, e);
            }
            // 构建 FileInfo 对象
            return new FileInfo()
                    .setFileName(fileOptions.getFileName())
                    .setFileSize(FileUtils.byteCountToDisplaySize(fileSize))
                    .setStoragePath(path.toString())
                    .setPlatform(fileOptions.getPlatform())
                    .setPlatformConfigName(fileOptions.getPlatformConfigName())
                    .setInputStream(fileStream) // 调用者负责关闭
                    .setContentType(contentType)
                    .setUploadTime(LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault()));
        } catch (NoSuchFileException e) {
            throw new FileStorageException("文件不存在: " + path, e);
        } catch (IOException e) {
            throw new FileStorageException("文件操作失败: " + path, e);
        }
    }


    /**
     * 删除指定文件
     *
     * @param fileOptions 文件选项，包含文件的存储路径和名称等信息
     * @return 如果文件成功删除，则返回true
     * @throws FileStorageException 如果文件删除失败，则抛出此异常
     */
    @Override
    public Boolean deleteFile(FileOptions fileOptions) {
        // 获取存储配置
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        // 生成文件的绝对路径
        Path path = Paths.get(PathUtil.mergePath(PathUtil.SlashType.BACK_SLASH, true, localStorageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName())).toAbsolutePath();
        try {
            // 尝试删除文件，如果文件不存在，则抛出异常
            if (!Files.deleteIfExists(path)) {
                throw new FileStorageException("要删除的文件不存在");
            }
            // 文件成功删除，返回true
            return true;
        } catch (IOException e) {
            // 文件删除失败，抛出自定义异常
            throw new FileStorageException("文件删除失败", e);
        }
    }

    /**
     * 判断文件是否存在于本地存储中
     *
     * @param fileOptions 文件选项，包含文件路径和名称等信息
     * @return 如果文件存在且不是一个目录，则返回true；否则返回false
     */
    @Override
    public Boolean exists(FileOptions fileOptions) {
        // 获取文件存储属性
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        // 构建文件的绝对路径
        Path path = Paths.get(PathUtil.mergePath(PathUtil.SlashType.BACK_SLASH, true, localStorageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName())).toAbsolutePath();
        // 检查文件是否存在且不是一个目录
        return Files.exists(path) && !Files.isDirectory(path);
    }

    /**
     * 获取文件夹下的文件名列表
     *
     * @param fileOptions 文件选项，包含文件路径和名称等信息
     * @return 文件名称列表
     */
    @Override
    public List<String> list(FileOptions fileOptions) {
        // 获取文件存储属性
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        // 构建文件的绝对路径
        Path path = Paths.get(PathUtil.mergePath(PathUtil.SlashType.BACK_SLASH, true, localStorageProperties.getBasePath(), fileOptions.getStoragePath())).toAbsolutePath();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new FileStorageException("文件夹不存在或路径不是文件夹");
        }
        try (Stream<Path> list = Files.list(path)) {
            return list.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
        } catch (IOException e) {
            throw new FileStorageException("文件列表获取失败", e);
        }
    }

    /**
     * 获取文件预签名URL
     *
     * @param fileOptions 文件选项
     * @return 文件预签名URL
     */
    @Override
    public String getFilePreSignedUrl(FileOptions fileOptions) {
        throw new FileStorageException("此存储不支持获取文件预签名URL");
    }

    /**
     * 获取文件预签名URL
     *
     * @param fileOptions 文件选项
     * @param expire      有效期
     * @param timeUnit    有效期单位
     * @return 文件预签名URL
     */
    @Override
    public String getFilePreSignedUrl(FileOptions fileOptions, Integer expire, TimeUnit timeUnit) {
        throw new FileStorageException("此存储不支持获取文件预签名URL");
    }
}
