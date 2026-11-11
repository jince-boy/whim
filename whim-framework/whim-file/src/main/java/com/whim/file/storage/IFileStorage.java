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

    /**
     * 保存经过FileSource校验的文件，云存储必须使用二进制附件响应属性。
     *
     * @param source 已校验的文件输入，资源由上层关闭
     * @param key 文件存储标识
     * @param contentType 内容类型声明，不能覆盖实际检测结果
     * @return 文件元数据
     */
    MetaData upload(FileSource source, String key, String contentType);

    /** 打开下载流，调用方必须关闭返回值。 */
    DownloadHandler download(String key);

    /** 获取文件元数据；对象不存在时抛出异常。 */
    MetaData getFileMetaData(String key);

    /** 判断对象存在，鉴权及服务端故障必须抛出异常。 */
    boolean exists(String key);

    /** 幂等删除对象；对象已经不存在也视为成功。 */
    void deleteFile(String key);

    /**
     * 获取强制附件下载签名，不支持的存储明确拒绝操作。
     *
     * @param key 文件存储标识
     * @param expiration 签名有效期
     * @return 强制二进制附件响应的下载地址
     */
    default String getFilePreSignedUrl(String key, Duration expiration) {
        throw new FileStorageException("当前存储不支持下载预签名地址");
    }

    /**
     * 拒绝没有内容校验完成环节的单次直传。
     *
     * @param key 文件存储标识
     * @param contentType 文件内容类型
     * @param expiration 签名有效期
     * @return 本方法始终拒绝签发并抛出异常
     */
    default String uploadFilePreSignedUrl(String key, String contentType, Duration expiration) {
        throw new FileStorageException("单次直传无法校验文件内容，请使用普通上传或分片上传");
    }

    /** 幂等释放该实例拥有的客户端资源；自定义 Bean 由 Spring 负责关闭。 */
    @Override
    default void close() {
        // 本地存储等无共享资源的实现无需清理。
    }
}
