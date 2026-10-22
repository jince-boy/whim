package com.whim.file.storage;

import com.whim.core.exception.FileStorageException;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;

import java.time.Duration;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 单个存储实例的操作契约，key 始终相对配置根目录或前缀。
 */
public interface IFileStorage extends AutoCloseable {
    /** 返回平台名称。 */
    String getPlatform();

    /** 返回平台内唯一的配置名称。 */
    String getConfigName();

    /** 上传文件，输入资源由上层服务关闭。 */
    MetaData upload(FileSource source, String key, String contentType);

    /** 打开下载流，调用方必须关闭返回值。 */
    DownloadHandler download(String key);

    /** 获取文件元数据；对象不存在时抛出异常。 */
    MetaData getFileMetaData(String key);

    /** 判断对象存在，鉴权及服务端故障必须抛出异常。 */
    boolean exists(String key);

    /** 幂等删除对象；对象已经不存在也视为成功。 */
    void deleteFile(String key);

    /** 获取下载签名；不支持的存储明确拒绝操作。 */
    default String getFilePreSignedUrl(String key, Duration expiration) {
        throw new FileStorageException("当前存储不支持下载预签名地址");
    }

    /** 获取上传签名；调用方必须使用相同 Content-Type 上传。 */
    default String uploadFilePreSignedUrl(String key, String contentType, Duration expiration) {
        throw new FileStorageException("当前存储不支持上传预签名地址");
    }

    /** 幂等释放该实例拥有的客户端资源；自定义 Bean 由 Spring 负责关闭。 */
    @Override
    default void close() {
        // 本地存储等无共享资源的实现无需清理。
    }
}
