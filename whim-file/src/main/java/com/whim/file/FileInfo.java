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


}
