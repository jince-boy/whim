package com.whim.file.client;

/**
 * @author jince
 * @date 2025/2/23 16:54
 * @description 文件存储客户端工厂
 */
public interface IFileStorageClientFactory<T> extends AutoCloseable {

    T getClient();

    @Override
    void close();
}
