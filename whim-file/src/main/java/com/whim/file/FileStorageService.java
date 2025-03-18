package com.whim.file;

import com.whim.file.adapter.IFileAdapter;
import com.whim.file.config.FileStorageProperties;
import com.whim.file.model.FileInfo;
import com.whim.file.storage.IFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author jince
 * date: 2025/2/18 13:47
 * description: 文件存储服务
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileStorageProperties fileStorageProperties;
    private final List<IFileAdapter> allFileAdapter;
    private final Map<String, IFileStorage> allFileStorage;

    /**
     * 上传文件基础方法（简化版）
     *
     * @param file 要上传的文件对象，支持多种文件类型包装
     * @return Boolean 上传结果，true表示成功
     */
    public Boolean upload(Object file) {
        return this.upload(file, null);
    }

    /**
     * 完整文件上传方法
     *
     * @param file         要上传的文件对象，支持多种文件类型包装
     * @param configurator 文件配置构建器（可选），用于自定义配置项
     * @return Boolean 上传结果，true表示成功
     * 实现逻辑：
     * 1. 初始化配置构建器并包装文件对象
     * 2. 应用自定义配置（如果存在）
     * 3. 构建最终文件配置选项
     * 4. 根据平台选择对应存储实现执行上传
     */
    public Boolean upload(Object file, Consumer<FileOptions.Builder> configurator) {
        FileOptions.Builder builder = new FileOptions.Builder(fileStorageProperties, allFileAdapter, allFileStorage);
        builder.fileWrapper(file);
        // 应用自定义配置
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }
        FileOptions fileOptions = builder.build();
        // 根据平台选择存储实现执行上传
        return allFileStorage.get(fileOptions.getPlatform()).upload(fileOptions);
    }

    /**
     * 获取文件信息
     *
     * @param configurator 文件配置构建器，用于指定要获取信息的文件参数
     * @return FileInfo 包含文件详细信息的数据对象
     * 实现逻辑：
     * 1. 初始化配置构建器
     * 2. 应用自定义文件参数配置
     * 3. 根据平台选择对应存储实现获取文件信息
     */
    public FileInfo getFileInfo(Consumer<FileOptions.Builder> configurator) {
        FileOptions.Builder builder = new FileOptions.Builder(fileStorageProperties, allFileAdapter, allFileStorage);
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }
        FileOptions fileOptions = builder.build();
        return allFileStorage.get(fileOptions.getPlatform()).getFileInfo(fileOptions);
    }

    /**
     * 删除文件方法
     * 该方法通过一个配置器来接收文件选项的配置，然后根据这些配置尝试删除指定的文件
     *
     * @param configurator 文件选项的配置器，用于定制文件删除操作的参数，如平台、文件路径等
     * @return 返回一个布尔值，表示文件是否删除成功
     */
    public Boolean deleteFile(Consumer<FileOptions.Builder> configurator) {
        // 创建一个文件选项构建器，用于配置文件操作的参数
        FileOptions.Builder builder = new FileOptions.Builder(fileStorageProperties, allFileAdapter, allFileStorage);

        // 如果配置器不为空，则使用配置器对构建器进行配置
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }

        // 根据配置好的构建器创建一个文件选项实例
        FileOptions fileOptions = builder.build();

        // 根据文件选项中的平台信息，获取对应的文件存储服务，并尝试删除文件
        return allFileStorage.get(fileOptions.getPlatform()).deleteFile(fileOptions);
    }

    /**
     * 检查文件是否存在
     * 通过配置器配置文件选项，并根据这些选项检查文件是否存在
     *
     * @param configurator 文件选项的配置器，用于定制文件选项
     * @return 文件是否存在如果文件存在返回true，否则返回false
     */
    public Boolean exists(Consumer<FileOptions.Builder> configurator) {
        // 创建文件选项构建器，用于配置文件选项
        FileOptions.Builder builder = new FileOptions.Builder(fileStorageProperties, allFileAdapter, allFileStorage);

        // 如果配置器不为空，应用配置器以配置文件选项
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }

        // 构建最终的文件选项
        FileOptions fileOptions = builder.build();

        // 根据文件选项检查文件是否存在
        return allFileStorage.get(fileOptions.getPlatform()).exists(fileOptions);
    }

    /**
     * 使用给定的配置器列出文件
     *
     * @param configurator 文件选项的配置器，用于定制文件查询条件
     * @return 包含文件名称的列表
     */
    public List<String> list(Consumer<FileOptions.Builder> configurator) {
        // 创建文件选项构建器，用于配置文件查询参数
        FileOptions.Builder builder = new FileOptions.Builder(fileStorageProperties, allFileAdapter, allFileStorage);

        // 如果配置器不为空，则应用配置器以设置文件选项
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }

        // 构建最终的文件选项实例
        FileOptions fileOptions = builder.build();

        // 使用配置好的文件选项来获取并返回文件列表
        return allFileStorage.get(fileOptions.getPlatform()).list(fileOptions);
    }

    /**
     * 生成预签名的文件URL，允许通过配置器设置文件查询参数
     * 此方法重载允许省略过期时间参数
     *
     * @param configurator 配置器，用于设置文件查询参数
     * @see #getFilePreSignedUrl(Consumer, Integer, TimeUnit)
     */
    public String getFilePreSignedUrl(Consumer<FileOptions.Builder> configurator) {
        return this.getFilePreSignedUrl(configurator, null, null);
    }

    /**
     * 生成预签名的文件URL，允许通过配置器设置文件查询参数，并指定URL的过期时间
     * 此方法允许用户自定义文件查询参数，以及设置URL的过期时间，提供更灵活的文件访问控制
     *
     * @param configurator 配置器，用于设置文件查询参数
     * @param expire       URL的过期时间
     * @param timeUnit     过期时间的时间单位
     * @return 预签名的文件URL
     */
    public String getFilePreSignedUrl(Consumer<FileOptions.Builder> configurator, Integer expire, TimeUnit timeUnit) {
        // 创建文件选项构建器，用于配置文件查询参数
        FileOptions.Builder builder = new FileOptions.Builder(fileStorageProperties, allFileAdapter, allFileStorage);
        // 如果配置器不为空，则应用配置器以设置文件选项
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }
        // 构建最终的文件选项实例
        FileOptions fileOptions = builder.build();
        // 使用配置好的文件选项来获取并返回文件列表
        return allFileStorage.get(fileOptions.getPlatform()).getFilePreSignedUrl(fileOptions, expire, timeUnit);
    }


    /**
     * 通过预签名URL上传文件
     *
     * @param configurator 文件选项的配置器，用于定制文件上传的选项
     * @param expire 预签名URL的过期时间
     * @param timeUnit 过期时间的时间单位
     * @return 上传文件的预签名URL
     */
    public String uploadFilePreSignedUrl(Consumer<FileOptions.Builder> configurator, Integer expire, TimeUnit timeUnit) {
        // 创建文件选项构建器，用于配置文件查询参数
        FileOptions.Builder builder = new FileOptions.Builder(fileStorageProperties, allFileAdapter, allFileStorage);
        // 如果配置器不为空，则应用配置器以设置文件选项
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }
        // 构建最终的文件选项实例
        FileOptions fileOptions = builder.build();
        // 使用配置好的文件选项来获取并返回文件列表
        return allFileStorage.get(fileOptions.getPlatform()).uploadFilePreSignedUrl(fileOptions, expire, timeUnit);
    }



}
