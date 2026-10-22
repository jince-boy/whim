package com.whim.file.storage;

import com.whim.file.model.MetaData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 存储实例身份与统一元数据构建。
 */
@Getter
@RequiredArgsConstructor
public abstract class BaseFileStorage implements IFileStorage {
    private final String platform;
    private final String configName;

    /** 将存储返回的信息转换为可直接再次定位的元数据。 */
    protected MetaData createMetaData(String key, long size, String contentType) {
        int separator = key.lastIndexOf('/');
        MetaData metadata = new MetaData();
        metadata.setPlatform(platform);
        metadata.setPlatformConfigName(configName);
        metadata.setObjectKey(key);
        metadata.setStoragePath(separator < 0 ? "" : key.substring(0, separator));
        metadata.setFileName(key.substring(separator + 1));
        metadata.setFileSize(size);
        metadata.setContentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType);
        return metadata;
    }
}
