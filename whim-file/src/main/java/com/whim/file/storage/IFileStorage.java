package com.whim.file.storage;

import com.whim.file.FileInfo;
import com.whim.file.wrapper.IFileWrapper;

/**
 * @author Jince
 * date: 2024/11/15 22:11
 * description:
 */
public interface IFileStorage {
    /**
     * 上传文件
     *
     * @param fileWrapper 文件包装器
     * @param fileInfo    文件上传信息
     * @return 上传结果 true 成功,false 失败
     */
    Boolean upload(IFileWrapper fileWrapper, FileInfo fileInfo);
}
