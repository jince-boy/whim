package com.whim.file.model;

import lombok.Data;

/**
 * @author jince
 * @date 2025/3/24 19:12
 * @description 文件元数据类，用于存储文件的相关信息
 */
@Data
public class MetaData {
    /**
     * 文件名，用于标识文件的名称
     */
    private String fileName;

    /**
     * 文件路径，表示文件在系统中的位置
     */
    private String storagePath;

    /**
     * 文件大小，用于描述文件的字节大小
     */
    private Long fileSize;

    /**
     * 文件内容类型，表示文件的MIME类型，用于在网络传输时标识文件类型
     */
    private String contentType;
}
