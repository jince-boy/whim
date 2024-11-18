package com.whim.file;

import lombok.Data;

/**
 * @author Jince
 * date: 2024/11/15 23:30
 * description: 文件上传信息
 */
@Data
public class FileInfo {
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件原始名称
     */
    private String originalFileName;
    /**
     * 基础路径
     */
    private String basePath;
    /**
     * 存储路径
     */
    private String storagePath;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件后缀
     */
    private String extension;
    /**
     * 文件类型
     */
    private String contentType;
    /**
     * 存储平台
     */
    private String storagePlatform;

}
