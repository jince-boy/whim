package com.whim.file.storage;

import com.whim.file.FileOptions;
import com.whim.file.model.vo.FileInfoVO;

import java.io.InputStream;

/**
 * @author jince
 * date: 2025/2/18 17:48
 * description: 文件存储服务
 */
public interface IFileStorage {
    /**
     * 上传文件
     *
     * @param fileOptions 文件选项
     * @return FileInfoVO
     */
    FileInfoVO upload(FileOptions fileOptions);

    /**
     * 获取文件
     * @param fileOptions 文件选项
     * @return InputStream
     */
    InputStream getFile(FileOptions fileOptions);
}
