package com.whim.file.storage.impl;

import com.whim.common.exception.FileStorageException;
import com.whim.common.utils.PathUtil;
import com.whim.file.FileOptions;
import com.whim.file.wrapper.IFileWrapper;
import com.whim.file.config.FileStorageProperties.LocalStorageProperties;
import com.whim.file.model.MetaData;
import com.whim.file.storage.IFileStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * @author jince
 * date: 2025/2/18 17:48
 * description: 本地文件存储实现
 */
@Component("local")
@Slf4j
public class LocalFileStorageImpl implements IFileStorage {

    /**
     * 上传文件方法
     * 该方法负责将给定的文件选项对象中的文件上传到本地存储系统，并返回文件的元数据
     * 它处理文件路径的创建、文件的上传、以及收集文件属性等任务
     *
     * @param fileOptions 文件选项对象，包含文件上传所需的各项配置和文件包装器
     * @return 返回上传文件的元数据，包括文件名、存储路径、文件大小、内容类型、创建时间和最后修改时间
     * @throws FileStorageException 如果文件上传过程中发生任何错误，将抛出此自定义异常
     */
    @Override
    public MetaData upload(FileOptions fileOptions) {
        // 获取本地存储属性
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        try (IFileWrapper fileWrapper = fileOptions.getFileWrapper()) {
            // 构建文件的绝对路径
            Path basePath = Paths.get(PathUtil.mergePath(PathUtil.SlashType.BACK_SLASH, true, localStorageProperties.getBasePath(), fileOptions.getStoragePath())).toAbsolutePath();
            // 如果路径不存在，则创建目录
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
            }
            // 解析完整的文件路径
            Path filePath = basePath.resolve(fileOptions.getFileName()).normalize();
            // 从文件包装器获取输入流
            InputStream inputStream = fileWrapper.getInputStream();
            // 将输入流中的数据复制到目标文件路径，替换已存在的文件
            long copy = Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            // 探测文件的内容类型
            String mimeType = Files.probeContentType(filePath);
            // 创建并填充元数据对象
            MetaData metaData = new MetaData();
            metaData.setFileName(fileOptions.getFileName());
            metaData.setStoragePath(filePath.toString());
            metaData.setFileSize(FileUtils.byteCountToDisplaySize(copy));
            metaData.setContentType(mimeType);
            // 返回元数据
            return metaData;
        } catch (Exception e) {
            // 如果上传过程中发生异常，抛出自定义异常
            throw new FileStorageException("文件上传失败:" + e.getMessage(), e);
        }
    }


    /**
     * 获取文件信息
     * 该方法根据文件选项构建文件包装器，并从本地存储中获取文件流
     * 主要用于处理文件上传、下载等操作中的文件信息获取
     *
     * @param fileOptions 文件选项对象，包含文件存储属性和文件包装器
     * @return 返回文件的输入流，用于读取文件内容
     * @throws FileStorageException 当文件不存在或获取文件时发生异常时抛出
     */
    @Override
    public InputStream getFileInfo(FileOptions fileOptions) {
        // 获取文件存储属性
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        try {
            // 构建文件路径并获取绝对路径
            Path path = Paths.get(PathUtil.mergePath(PathUtil.SlashType.BACK_SLASH, true, localStorageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName())).toAbsolutePath();
            // 返回文件输入流
            return new BufferedInputStream(Files.newInputStream(path));
        } catch (NoSuchFileException e) {
            // 当文件不存在时抛出异常
            throw new FileStorageException("文件不存在", e);
        } catch (Exception e) {
            // 当获取文件发生其他异常时抛出异常
            throw new FileStorageException("文件获取失败:" + e.getMessage(), e);
        }
    }


    /**
     * 删除指定文件
     *
     * @param fileOptions 文件选项，包含文件的必要信息和配置
     * @return 如果文件删除成功，则返回true
     * @throws FileStorageException 如果文件删除失败，则抛出此异常
     */
    @Override
    public Boolean deleteFile(FileOptions fileOptions) {
        // 获取存储配置
        LocalStorageProperties localStorageProperties = (LocalStorageProperties) fileOptions.getStorageProperties();
        try {
            // 构建文件的绝对路径
            Path path = Paths.get(PathUtil.mergePath(PathUtil.SlashType.BACK_SLASH, true, localStorageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName())).toAbsolutePath();
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
        try {
            // 构建文件的绝对路径
            Path path = Paths.get(PathUtil.mergePath(PathUtil.SlashType.BACK_SLASH, true, localStorageProperties.getBasePath(), fileOptions.getStoragePath(), fileOptions.getFileName())).toAbsolutePath();
            // 检查文件是否存在且不是一个目录
            return Files.exists(path) && !Files.isDirectory(path);
        } catch (Exception e) {
            throw new FileStorageException("查找文件异常", e);
        }

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
        throw new FileStorageException("此存储不支持获取文件预签名URL");
    }
}
