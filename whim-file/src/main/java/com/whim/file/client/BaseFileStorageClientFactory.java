package com.whim.file.client;

import lombok.Getter;

/**
 * @author jince
 * date: 2025/3/18 21:52
 * description: 文件存储客户端工厂
 */
@Getter
public abstract class BaseFileStorageClientFactory<T> implements IFileStorageClientFactory<T> {
    protected final String name;
    protected final String url;
    protected final String accessKey;
    protected final String secretKey;
    protected final String bucket;
    protected final String basePath;
    protected volatile T client;

    public BaseFileStorageClientFactory(String name, String url, String accessKey, String secretKey, String bucket, String basePath) {
        this.name = name;
        this.url = url;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.basePath = basePath;
    }
}
