package com.whim.file.storage;

import com.whim.file.FileHandler;

/**
 * @author Jince
 * date: 2024/11/15 22:11
 * description:
 */
public interface IFileStorage {
    /**
     * 上传文件
     *
     * @param fileHandler 文件处理器
     * @return 上传结果 true 成功,false 失败
     */
    Boolean upload(FileHandler fileHandler);
}
