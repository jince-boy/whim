package com.whim.file.storage.impl;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.whim.core.exception.FileStorageException;
import com.whim.core.utils.PathUtils;
import com.whim.file.client.AliyunOssFileStorageClientFactory;
import com.whim.file.config.properties.FileStorageProperties.AliYunOssStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.handler.FileHandler;
import com.whim.file.model.MetaData;
import com.whim.file.storage.IFileStorage;
import com.whim.file.wrapper.IFileWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author jince
 * date: 2025/3/18 21:22
 * description: 阿里云oss文件存储实现
 */
@Slf4j
public class AliyunOssFileStorageImpl implements IFileStorage {

    /**
     * 上传文件到阿里云OSS服务
     *
     * @param fileHandler 文件上传选项，包含文件存储属性和文件包装器
     * @return 返回上传文件的元数据
     * @throws FileStorageException 如果文件上传过程中发生错误
     */
    @Override
    public MetaData upload(FileHandler fileHandler) {
        // 获取阿里云OSS存储属性
        AliYunOssStorageProperties aliYunOssStorageProperties = (AliYunOssStorageProperties) fileHandler.getStorageProperties();
        try (AliyunOssFileStorageClientFactory storageClientFactory = new AliyunOssFileStorageClientFactory(aliYunOssStorageProperties)) {
            try (IFileWrapper fileWrapper = fileHandler.getFileWrapper()) {
                // 构造文件在OSS中的完整路径
                String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, aliYunOssStorageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
                // 获取文件输入流
                InputStream inputStream = fileWrapper.getInputStream();
                // 创建并设置对象元数据
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentType(fileHandler.getContentType());
                // 创建上传请求对象
                PutObjectRequest putObjectRequest = new PutObjectRequest(aliYunOssStorageProperties.getBucket(), path, inputStream, objectMetadata);
                // 执行文件上传
                storageClientFactory.getClient().putObject(putObjectRequest);

                // 获取上传后的文件完整元数据
                ObjectMetadata fullMetadata = storageClientFactory.getClient().getObjectMetadata(aliYunOssStorageProperties.getBucket(), path);
                // 创建并填充元数据对象
                MetaData metaData = new MetaData();
                metaData.setFileName(fileHandler.getFileName());
                metaData.setStoragePath(path);
                metaData.setFileSize(fullMetadata.getContentLength());
                metaData.setContentType(fullMetadata.getContentType());
                // 返回元数据
                return metaData;
            }
        } catch (Exception e) {
            // 如果上传过程中发生错误，抛出自定义异常
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
    @SuppressWarnings("resource")
    public DownloadHandler download(FileHandler fileHandler) {
        // 获取阿里云OSS存储属性
        AliYunOssStorageProperties properties = (AliYunOssStorageProperties) fileHandler.getStorageProperties();
        AliyunOssFileStorageClientFactory factory = new AliyunOssFileStorageClientFactory(properties);
        // 构造文件在OSS中的完整路径
        String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, properties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
        // 检查文件是否存在
        if (factory.getClient().doesObjectExist(properties.getBucket(), path)) {
            // 获取文件对象
            OSSObject ossObject = factory.getClient().getObject(properties.getBucket(), path);
            BufferedInputStream objectContent = new BufferedInputStream(ossObject.getObjectContent());
            return DownloadHandler.of(objectContent, () -> {
                try {
                    objectContent.close();
                    ossObject.close();
                    factory.close();
                } catch (Exception e) {
                    throw new FileStorageException("资源关闭失败", e);
                }
            });
        } else {
            throw new FileStorageException("文件不存在");
        }
    }

    /**
     * 获取文件的元数据信息
     *
     * @param fileHandler 文件处理器，包含文件的存储路径和名称等信息
     * @return 返回文件的元数据对象，包括文件名、存储路径、文件大小和内容类型
     * @throws FileStorageException 如果文件不存在，则抛出文件存储异常
     */
    @Override
    public MetaData getFileMetaData(FileHandler fileHandler) {
        // 获取阿里云OSS存储属性
        AliYunOssStorageProperties properties = (AliYunOssStorageProperties) fileHandler.getStorageProperties();
        try (AliyunOssFileStorageClientFactory factory = new AliyunOssFileStorageClientFactory(properties)) {
            // 构造文件在OSS中的完整路径
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, properties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            // 检查文件是否存在
            if (factory.getClient().doesObjectExist(properties.getBucket(), path)) {
                // 获取上传后的文件完整元数据
                ObjectMetadata fullMetadata = factory.getClient().getObjectMetadata(properties.getBucket(), path);
                // 创建并填充元数据对象
                MetaData metaData = new MetaData();
                metaData.setFileName(fileHandler.getFileName());
                metaData.setStoragePath(path);
                metaData.setFileSize(fullMetadata.getContentLength());
                metaData.setContentType(fullMetadata.getContentType());
                // 返回元数据
                return metaData;
            }
            // 如果文件不存在，抛出异常
            throw new FileStorageException("文件不存在");
        }
    }


    /**
     * 删除文件方法
     *
     * @param fileHandler 文件选项，包含文件存储属性和文件包装器
     * @return 文件删除成功返回true，否则抛出异常
     * @throws FileStorageException 如果文件删除失败，则抛出此异常
     */
    @Override
    public Boolean deleteFile(FileHandler fileHandler) {
        // 获取阿里云OSS存储属性
        AliYunOssStorageProperties aliYunOssStorageProperties = (AliYunOssStorageProperties) fileHandler.getStorageProperties();
        // 创建阿里云OSS文件存储客户端工厂，并确保资源在使用后被正确关闭
        try (AliyunOssFileStorageClientFactory storageClientFactory = new AliyunOssFileStorageClientFactory(aliYunOssStorageProperties)) {
            // 构造文件的完整路径
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, aliYunOssStorageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            if (storageClientFactory.getClient().doesObjectExist(aliYunOssStorageProperties.getBucket(), path)) {
                // 使用阿里云OSS客户端删除指定桶中的对象
                storageClientFactory.getClient().deleteObject(aliYunOssStorageProperties.getBucket(), path);
                // 文件删除成功，返回true
                return true;
            }
            throw new FileStorageException("文件不存在");
        } catch (FileStorageException e) {
            // 如果文件存储操作中出现已知异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 如果出现其他异常，包装为文件存储异常并抛出
            throw new FileStorageException("文件删除失败:" + e.getMessage(), e);
        }
    }


    /**
     * 检查文件是否存在于阿里云OSS存储中
     *
     * @param fileHandler 文件选项，包含文件存储属性和文件包装对象
     * @return 如果文件存在，则返回true；否则返回false
     * @throws FileStorageException 如果文件查找过程中发生异常
     */
    @Override
    public Boolean exists(FileHandler fileHandler) {
        // 获取阿里云OSS存储属性
        AliYunOssStorageProperties aliYunOssStorageProperties = (AliYunOssStorageProperties) fileHandler.getStorageProperties();
        try (AliyunOssFileStorageClientFactory storageClientFactory = new AliyunOssFileStorageClientFactory(aliYunOssStorageProperties)) {
            // 构造文件的完整路径
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, aliYunOssStorageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            // 检查文件是否存在于指定的桶和路径中
            return storageClientFactory.getClient().doesObjectExist(aliYunOssStorageProperties.getBucket(), path);
        } catch (Exception e) {
            // 如果查找文件时发生异常，抛出自定义文件存储异常
            throw new FileStorageException("查找文件异常", e);
        }
    }


    /**
     * 获取文件的预签名URL
     *
     * @param fileHandler 文件选项，包含文件存储属性和文件包装器
     * @param expire      预签名URL的过期时间
     * @param timeUnit    时间单位，用于指定过期时间的度量
     * @return 预签名URL字符串，用于访问存储在OSS中的文件
     * @throws FileStorageException 如果获取预签名URL失败，则抛出此异常
     */
    @Override
    public String getFilePreSignedUrl(FileHandler fileHandler, Integer expire, TimeUnit timeUnit) {
        // 获取文件存储属性，这里是阿里云OSS的存储属性
        AliYunOssStorageProperties aliYunOssStorageProperties = (AliYunOssStorageProperties) fileHandler.getStorageProperties();
        try (AliyunOssFileStorageClientFactory storageClientFactory = new AliyunOssFileStorageClientFactory(aliYunOssStorageProperties)) {

            // 合并基础路径、文件存储路径和文件名，生成完整路径
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, aliYunOssStorageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            if (storageClientFactory.getClient().doesObjectExist(aliYunOssStorageProperties.getBucket(), path)) {
                // 计算过期时间：当前时间 + 指定的 expire 时间
                long expirationMillis = System.currentTimeMillis() + timeUnit.toMillis(expire);
                Date expiration = new Date(expirationMillis); // 转换为 Date 对象
                // 生成预签名 URL
                URL url = storageClientFactory.getClient().generatePresignedUrl(aliYunOssStorageProperties.getBucket(), path, expiration);
                // 对 URL 进行 UTF-8 解码，确保 URL 中包含特殊字符时能够正确处理
                return URLDecoder.decode(url.toString(), StandardCharsets.UTF_8);
            }
            throw new FileStorageException("文件不存在");
        } catch (Exception e) {
            // 如果获取预签名URL失败，抛出自定义异常
            throw new FileStorageException("文件预签名URL获取失败:" + e.getMessage(), e);
        }
    }


    /**
     * 使用预签名URL上传文件到阿里云OSS
     *
     * @param fileHandler 文件选项，包含文件存储属性和文件包装器
     * @param expire      预签名URL的过期时间
     * @param timeUnit    过期时间的时间单位
     * @return 上传文件的预签名URL
     * @throws FileStorageException 如果获取预签名URL失败，抛出此异常
     */
    @Override
    public String uploadFilePreSignedUrl(FileHandler fileHandler, Integer expire, TimeUnit timeUnit) {
        // 获取阿里云OSS存储属性
        AliYunOssStorageProperties aliYunOssStorageProperties = (AliYunOssStorageProperties) fileHandler.getStorageProperties();
        try (AliyunOssFileStorageClientFactory storageClientFactory = new AliyunOssFileStorageClientFactory(aliYunOssStorageProperties)) {
            // 构建文件路径
            String path = PathUtils.mergePath(PathUtils.SlashType.FORWARD_SLASH, false, false, aliYunOssStorageProperties.getBasePath(), fileHandler.getStoragePath(), fileHandler.getFileName());
            // 计算过期时间：当前时间 + 指定的 expire 时间
            long expirationMillis = System.currentTimeMillis() + timeUnit.toMillis(expire);
            Date expiration = new Date(expirationMillis); // 转换为 Date 对象
            // 生成预签名 URL
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(aliYunOssStorageProperties.getBucket(), path, HttpMethod.PUT);
            // 设置过期时间。
            request.setExpiration(expiration);
            request.setContentType(fileHandler.getContentType());
            // 通过HTTP PUT请求生成预签名URL。
            URL url = storageClientFactory.getClient().generatePresignedUrl(request);
            // 对URL进行UTF-8解码，防止特殊字符问题
            return URLDecoder.decode(url.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 如果获取预签名URL失败，抛出自定义异常
            throw new FileStorageException("文件预签名URL获取失败:" + e.getMessage(), e);
        }
    }

}
