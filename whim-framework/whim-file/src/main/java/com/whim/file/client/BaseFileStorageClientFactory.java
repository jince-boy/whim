package com.whim.file.client;

import lombok.Getter;

/**
 * @author jince
 * @date 2025/3/18 21:52
 * @description 文件存储客户端工厂
 */
@Getter
public abstract class BaseFileStorageClientFactory<T> implements IFileStorageClientFactory<T> {
    protected final String url;
    protected final String accessKey;
    protected final String secretKey;
    protected final String region;
    protected volatile T client;

    public BaseFileStorageClientFactory(String url, String accessKey, String secretKey, String region) {
        this.url = url;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }
}
