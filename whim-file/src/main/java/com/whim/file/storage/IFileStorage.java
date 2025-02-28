package com.whim.file.storage;

import com.whim.file.FileOptions;

/**
 * @author jince
 * date: 2025/2/18 17:48
 * description: 文件存储服务
 */
public interface IFileStorage {
    void upload(FileOptions fileOptions);
}
