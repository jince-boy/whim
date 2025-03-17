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

}
