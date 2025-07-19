package com.whim.file;

import com.whim.file.adapter.IFileAdapter;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.handler.FileHandler;
import com.whim.file.model.MetaData;
import com.whim.file.storage.IFileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author jince
 * @date 2025/2/18 13:47
 * @description 文件存储服务
 */
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {
    private final FileStorageProperties fileStorageProperties;
    private final List<IFileAdapter> allFileAdapter;
    private final Map<String, IFileStorage> allFileStorage;

    /**
     * 上传文件并返回元数据
     * 此方法简化了上传流程，使用默认的上传选项
     *
     * @param file 要上传的文件对象
     * @return 文件上传后的元数据对象
     */
    public MetaData upload(Object file) {
        return this.upload(file, null);
    }


    /**
     * 上传文件并返回元数据
     *
     * @param file         要上传的文件对象
     * @param configurator 文件上传的配置器，用于定制上传选项
     * @return 文件上传后的元数据
     */
    public MetaData upload(Object file, Consumer<FileHandler.Builder> configurator) {
        // 创建文件选项构建器
        FileHandler.Builder builder = new FileHandler.Builder(fileStorageProperties, allFileAdapter, allFileStorage);

        // 设置文件包装
        builder.fileWrapper(file);
        // 应用自定义配置
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }
        // 构建文件选项
        FileHandler fileHandler = builder.build();
        // 根据平台选择存储实现执行上传
        return allFileStorage.get(fileHandler.getPlatform()).upload(fileHandler);
    }


    /**
     * 获取文件信息
     * 该方法通过接受一个消费者函数接口（Consumer）来配置文件选项，然后根据这些选项获取文件信息
     * 它主要用于以一种灵活的方式获取文件的详细信息，比如元数据或者文件的某个特定部分
     *
     * @param configurator 文件选项的配置器，用于定制化文件选项
     * @return InputStream 文件信息的输入流，可以用于读取文件内容
     */
    public DownloadHandler download(Consumer<FileHandler.Builder> configurator) {
        // 创建一个文件选项构建器，初始化包含文件存储属性、文件适配器和文件存储
        FileHandler.Builder builder = new FileHandler.Builder(fileStorageProperties, allFileAdapter, allFileStorage);

        // 如果配置器不为空，则使用配置器配置文件选项构建器
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }

        // 构建最终的文件选项实例
        FileHandler fileHandler = builder.build();

        // 根据文件选项中的平台信息，获取对应平台的文件信息
        return allFileStorage.get(fileHandler.getPlatform()).download(fileHandler);
    }


    /**
     * 删除文件方法
     * 该方法通过一个配置器来接收文件选项的配置，然后根据这些配置尝试删除指定的文件
     *
     * @param configurator 文件选项的配置器，用于定制文件删除操作的参数，如平台、文件路径等
     * @return 返回一个布尔值，表示文件是否删除成功
     */
    public Boolean deleteFile(Consumer<FileHandler.Builder> configurator) {
        // 创建一个文件选项构建器，用于配置文件操作的参数
        FileHandler.Builder builder = new FileHandler.Builder(fileStorageProperties, allFileAdapter, allFileStorage);

        // 如果配置器不为空，则使用配置器对构建器进行配置
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }

        // 根据配置好的构建器创建一个文件选项实例
        FileHandler fileHandler = builder.build();

        // 根据文件选项中的平台信息，获取对应的文件存储服务，并尝试删除文件
        return allFileStorage.get(fileHandler.getPlatform()).deleteFile(fileHandler);
    }


    /**
     * 检查文件是否存在
     * 通过配置器配置文件选项，并根据这些选项检查文件是否存在
     *
     * @param configurator 文件选项的配置器，用于定制文件选项
     * @return 文件是否存在如果文件存在返回true，否则返回false
     */
    public Boolean exists(Consumer<FileHandler.Builder> configurator) {
        // 创建文件选项构建器，用于配置文件选项
        FileHandler.Builder builder = new FileHandler.Builder(fileStorageProperties, allFileAdapter, allFileStorage);

        // 如果配置器不为空，应用配置器以配置文件选项
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }

        // 构建最终的文件选项
        FileHandler fileHandler = builder.build();

        // 根据文件选项检查文件是否存在
        return allFileStorage.get(fileHandler.getPlatform()).exists(fileHandler);
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
    public String getFilePreSignedUrl(Consumer<FileHandler.Builder> configurator, Integer expire, TimeUnit timeUnit) {
        // 创建文件选项构建器，用于配置文件查询参数
        FileHandler.Builder builder = new FileHandler.Builder(fileStorageProperties, allFileAdapter, allFileStorage);
        // 如果配置器不为空，则应用配置器以设置文件选项
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }
        // 构建最终的文件选项实例
        FileHandler fileHandler = builder.build();
        // 使用配置好的文件选项来获取并返回文件列表
        return allFileStorage.get(fileHandler.getPlatform()).getFilePreSignedUrl(fileHandler, expire, timeUnit);
    }


    /**
     * 通过预签名URL上传文件
     *
     * @param configurator 文件选项的配置器，用于定制文件上传的选项
     * @param expire       预签名URL的过期时间
     * @param timeUnit     过期时间的时间单位
     * @return 上传文件的预签名URL
     */
    public String uploadFilePreSignedUrl(Consumer<FileHandler.Builder> configurator, Integer expire, TimeUnit timeUnit) {
        // 创建文件选项构建器，用于配置文件查询参数
        FileHandler.Builder builder = new FileHandler.Builder(fileStorageProperties, allFileAdapter, allFileStorage);
        // 如果配置器不为空，则应用配置器以设置文件选项
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }
        // 构建最终的文件选项实例
        FileHandler fileHandler = builder.build();
        // 使用配置好的文件选项来获取并返回文件列表
        return allFileStorage.get(fileHandler.getPlatform()).uploadFilePreSignedUrl(fileHandler, expire, timeUnit);
    }

    /**
     * 获取文件元数据
     * <p>
     * 该方法通过接受一个消费者函数接口（Consumer）来配置文件处理的选项，然后根据这些选项获取文件的元数据
     *
     * @param configurator 文件处理选项的配置器，通过Consumer接口允许调用者在文件处理前配置处理选项
     * @return MetaData 返回文件的元数据对象，包含文件的相关信息
     */
    public MetaData getFileMetaData(Consumer<FileHandler.Builder> configurator) {
        // 创建文件选项构建器，用于配置文件选项
        FileHandler.Builder builder = new FileHandler.Builder(fileStorageProperties, allFileAdapter, allFileStorage);

        // 如果配置器不为空，则应用配置器以设置文件选项
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }

        // 构建最终的文件选项实例
        FileHandler fileHandler = builder.build();

        // 根据构建的文件选项实例获取并返回文件元数据
        return allFileStorage.get(fileHandler.getPlatform()).getFileMetaData(fileHandler);
    }


}
