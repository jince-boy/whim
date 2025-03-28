package com.whim.file.storage;

import com.whim.file.handler.DownloadHandler;
import com.whim.file.handler.FileHandler;
import com.whim.file.model.MetaData;

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
     * @param fileHandler 文件选项
     * @return true 上传成功，false 上传失败
     */
    MetaData upload(FileHandler fileHandler);


    /**
     * 下载文件
     *
     * @param fileHandler 文件选项
     * @return DownloadHandler 文件下载处理器
     */
    DownloadHandler download(FileHandler fileHandler);

    /**
     * 获取文件元数据
     *
     * @param fileHandler 文件选项
     * @return MetaData 元信息
     */
    MetaData getFileMetaData(FileHandler fileHandler);

    /**
     * 删除文件
     *
     * @param fileHandler 文件选项
     * @return true 删除成功，false 删除失败
     */
    Boolean deleteFile(FileHandler fileHandler);

    /**
     * 判断文件是否存在
     *
     * @param fileHandler 文件选项
     * @return true 存在 false 不存在
     */
    Boolean exists(FileHandler fileHandler);

    /**
     * 获取文件预签名URL
     *
     * @param fileHandler 文件选项
     * @param expire      有效期
     * @return 预签名url
     */
    String getFilePreSignedUrl(FileHandler fileHandler, Integer expire, TimeUnit timeUnit);

    /**
     * 获取文件上传预签名URL
     *
     * @param fileHandler 文件选项
     * @param expire      有效期
     * @param timeUnit    时间单位
     * @return 预签名url
     */
    String uploadFilePreSignedUrl(FileHandler fileHandler, Integer expire, TimeUnit timeUnit);
}
