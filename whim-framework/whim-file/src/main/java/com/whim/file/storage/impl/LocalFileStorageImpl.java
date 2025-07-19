package com.whim.file.storage.impl;

import com.whim.core.exception.FileStorageException;
import com.whim.core.utils.PathUtils;
import com.whim.file.config.properties.FileStorageProperties.LocalStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.handler.FileHandler;
import com.whim.file.model.MetaData;
import com.whim.file.storage.IFileStorage;
import com.whim.file.wrapper.IFileWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * @author jince
 * @date 2025/2/18 17:48
 * @description 本地文件存储实现
 */
@Slf4j
public class LocalFileStorageImpl implements IFileStorage {

    /**
     * 上传文件方法
     * 该方法负责将给定的文件选项对象中的文件上传到本地存储系统，并返回文件的元数据
     * 它处理文件路径的创建、文件的上传、以及收集文件属性等任务
     *
     * @param fileHandler 文件选项对象，包含文件上传所需的各项配置和文件包装器
     * @return 返回上传文件的元数据，包括文件名、存储路径、文件大小、内容类型、创建时间和最后修改时间
     * @throws FileStorageException 如果文件上传过程中发生任何错误，将抛出此自定义异常
     */
    @Override
    public MetaData upload(FileHandler fileHandler) {
        // 获取本地存储属性
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileHandler.getStorageProperties();
        try (IFileWrapper fileWrapper = fileHandler.getFileWrapper()) {
            // 构建文件的绝对路径
            Path basePath = Paths.get(PathUtils.mergePath(PathUtils.SlashType.BACK_SLASH, true, true, localStorageProperties.getBasePath(), fileHandler.getStoragePath())).toAbsolutePath();
            // 如果路径不存在，则创建目录
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }
            // 解析完整的文件路径
            Path filePath = basePath.resolve(fileHandler.getFileName()).normalize();
            // 从文件包装器获取输入流
            InputStream inputStream = fileWrapper.getInputStream();
            // 将输入流中的数据复制到目标文件路径，替换已存在的文件
            long copy = Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            // 探测文件的内容类型
            String mimeType = Files.probeContentType(filePath);
            // 创建并填充元数据对象
            MetaData metaData = new MetaData();
            metaData.setFileName(fileHandler.getFileName());
            metaData.setStoragePath(filePath.toString());
            metaData.setFileSize(copy);
            metaData.setContentType(mimeType);
            // 返回元数据
            return metaData;
        } catch (Exception e) {
            // 如果上传过程中发生异常，抛出自定义异常
            throw new FileStorageException("文件上传失败:" + e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     *
     * @param fileHandler 文件选项
     * @return DownloadHandler 文件下载处理器
     */
    @Override
    public DownloadHandler download(FileHandler fileHandler) {
        // 获取文件存储属性
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileHandler.getStorageProperties();
        try {
            // 构建文件路径并获取绝对路径
            Path path = Paths.get(PathUtils.mergePath(PathUtils.SlashType.BACK_SLASH, true, false, localStorageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName())).toAbsolutePath();
            // 返回文件输入流
            BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path));
            return DownloadHandler.of(bufferedInputStream, () -> {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    throw new FileStorageException("资源关闭失败", e);
                }
            });
        } catch (NoSuchFileException e) {
            // 当文件不存在时抛出异常
            throw new FileStorageException("文件不存在", e);
        } catch (Exception e) {
            // 当获取文件发生其他异常时抛出异常
            throw new FileStorageException("文件获取失败:" + e.getMessage(), e);
        }
    }

    /**
     * 获取文件的元数据信息
     *
     * @param fileHandler 文件处理器，包含文件存储路径和文件名等信息
     * @return 文件的元数据信息，包括文件名、存储路径、文件大小和MIME类型
     * @throws FileStorageException 如果文件不存在或无法获取文件元信息时抛出异常
     */
    @Override
    public MetaData getFileMetaData(FileHandler fileHandler) {
        // 获取本地存储属性
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileHandler.getStorageProperties();

        // 构建文件的绝对路径
        Path basePath = Paths.get(PathUtils.mergePath(PathUtils.SlashType.BACK_SLASH, true, false, localStorageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName())).toAbsolutePath();

        // 检查文件是否存在且不是目录
        if (Files.exists(basePath) && !Files.isDirectory(basePath)) {
            try {
                // 创建MetaData对象以存储文件元信息
                MetaData metaData = new MetaData();
                metaData.setFileName(basePath.getFileName().toString());
                metaData.setStoragePath(basePath.toString());
                metaData.setFileSize(Files.size(basePath));
                // 获取文件 MIME 类型，若获取失败，默认 application/octet-stream
                String contentType = Files.probeContentType(basePath);
                metaData.setContentType(contentType != null ? contentType : "application/octet-stream");
                return metaData;
            } catch (Exception e) {
                // 处理在获取文件元信息过程中可能发生的异常
                throw new FileStorageException("获取文件元信息失败", e);
            }
        }
        // 如果文件不存在，抛出异常
        throw new FileStorageException("文件不存在");
    }


    /**
     * 删除指定文件
     *
     * @param fileHandler 文件选项，包含文件的必要信息和配置
     * @return 如果文件删除成功，则返回true
     * @throws FileStorageException 如果文件删除失败，则抛出此异常
     */
    @Override
    public Boolean deleteFile(FileHandler fileHandler) {
        // 获取存储配置
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileHandler.getStorageProperties();
        try {
            // 构建文件的绝对路径
            Path path = Paths.get(PathUtils.mergePath(PathUtils.SlashType.BACK_SLASH, true, false, localStorageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName())).toAbsolutePath();
            // 如果是目录，则禁止删除
            if (Files.isDirectory(path)) {
                throw new FileStorageException("此方法不支持删除文件夹");
            }
            // 删除文件
            if (!Files.deleteIfExists(path)) {
                throw new FileStorageException("要删除的文件不存在");
            }
            return true;
        } catch (Exception e) {
            throw new FileStorageException("文件删除失败:" + e.getMessage(), e);
        }
    }


    /**
     * 判断文件是否存在于本地存储中
     *
     * @param fileHandler 文件选项，包含文件路径和名称等信息
     * @return 如果文件存在且不是一个目录，则返回true；否则返回false
     */
    @Override
    public Boolean exists(FileHandler fileHandler) {
        // 获取文件存储属性
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileHandler.getStorageProperties();
        try {
            // 构建文件的绝对路径
            Path path = Paths.get(PathUtils.mergePath(PathUtils.SlashType.BACK_SLASH, true, false, localStorageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName())).toAbsolutePath();
            // 检查文件是否存在且不是一个目录
            return Files.exists(path) && !Files.isDirectory(path);
        } catch (Exception e) {
            throw new FileStorageException("查找文件异常", e);
        }

    }

    /**
     * 获取文件预签名URL
     *
     * @param fileHandler 文件选项
     * @param expire      有效期
     * @param timeUnit    有效期单位
     * @return 文件预签名URL
     */
    @Override
    public String getFilePreSignedUrl(FileHandler fileHandler, Integer expire, TimeUnit timeUnit) {
        throw new FileStorageException("此存储不支持获取文件预签名URL");
    }

    /**
     * 获取上传文件预签名URL
     *
     * @param fileHandler 文件选项
     * @param expire      有效期
     * @param timeUnit    有效期单位
     * @return 文件预签名URL
     */
    @Override
    public String uploadFilePreSignedUrl(FileHandler fileHandler, Integer expire, TimeUnit timeUnit) {
        throw new FileStorageException("此存储不支持获取文件预签名URL");
    }
}
