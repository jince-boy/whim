package com.whim.file.storage;

import com.whim.file.FileOptions;
import com.whim.file.model.MetaData;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

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
     * @return true 上传成功，false 上传失败
     */
    MetaData upload(FileOptions fileOptions);

    /**
     * 获取文件
     *
     * @param fileOptions 文件选项
     * @return 文件信息
     */
    InputStream getFileInfo(FileOptions fileOptions);

    /**
     * 删除文件
     *
     * @param fileOptions 文件选项
     * @return true 删除成功，false 删除失败
     */
    Boolean deleteFile(FileOptions fileOptions);

    /**
     * 判断文件是否存在
     *
     * @param fileOptions 文件选项
     * @return true 存在 false 不存在
     */
    Boolean exists(FileOptions fileOptions);

    /**
     * 获取文件预签名URL
     *
     * @param fileOptions 文件选项
     * @param expire      有效期
     * @return 预签名url
     */
    String getFilePreSignedUrl(FileOptions fileOptions, Integer expire, TimeUnit timeUnit);

    /**
     * 获取文件上传预签名URL
     *
     * @param fileOptions 文件选项
     * @param expire      有效期
     * @param timeUnit    时间单位
     * @return 预签名url
     */
    String uploadFilePreSignedUrl(FileOptions fileOptions, Integer expire, TimeUnit timeUnit);
}
